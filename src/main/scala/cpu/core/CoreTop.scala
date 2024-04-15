package cpu.core

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.utils._
import cpu.utils.StageConnect._
import cpu.common.Const._

import pipeline.components.decode._
import pipeline.components.execute._
import pipeline.components.fetch._
import pipeline.components.memory._
import pipeline.components.writeback._

class CoreTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    val iCache = new Bundle {
      val inst_sram_rdata = Input(UInt(INST_WIDTH.W))
      val inst_sram_en    = Output(Bool())
      val inst_sram_wen   = Output(UInt(WEN_WIDTH.W))
      val inst_sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val inst_sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
    val dCache = new Bundle {
      val data_sram_rdata = Input(UInt(DATA_WIDTH.W))
      val data_sram_en    = Output(Bool())
      val data_sram_wen   = Output(UInt(WEN_WIDTH.W))
      val data_sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val data_sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
    val debug = Output(new DebugInfo)
  })
  val fetchUnit     = Module(new FetchUnit)
  val decodeUnit    = Module(new DecodeUnit)
  val executeUnit   = Module(new ExecuteUnit)
  val memoryUnit    = Module(new MemoryUnit)
  val writebackUnit = Module(new WriteBackUnit)

  val fetch     = fetchUnit.io
  val decode    = decodeUnit.io
  val execute   = executeUnit.io
  val memory    = memoryUnit.io
  val writeback = writebackUnit.io

  stageConnect(fetch.out, decode.in)
  stageConnect(decode.out, execute.in)
  stageConnect(execute.out, memory.in)
  stageConnect(memory.out, writeback.in)

  io.resetn <> fetch.reset
  io.iCache.inst_sram_rdata <> fetch.iCache.inst_sram_rdata
  io.iCache.inst_sram_en <> fetch.iCache.inst_sram_en
  io.iCache.inst_sram_addr <> fetch.iCache.inst_sram_addr

  fetch.jinfo <> decode.jinfo

  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen <> decode.wb.wen

  writeback.debug <> io.debug

  io.iCache.inst_sram_wen   := 0.U
  io.iCache.inst_sram_wdata := 0.U

  io.dCache.data_sram_en    := false.B
  io.dCache.data_sram_wen   := 0.U
  io.dCache.data_sram_addr  := 0.U
  io.dCache.data_sram_wdata := 0.U
}
