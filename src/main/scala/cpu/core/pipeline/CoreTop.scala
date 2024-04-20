package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.utils._
import cpu.utils.StageConnect._
import cpu.common.Const._

import cpu.core.pipeline.components.fetch._
import cpu.core.pipeline.components.decode._
import cpu.core.pipeline.components.execute._
import cpu.core.pipeline.components.memory._
import cpu.core.pipeline.components.writeback._

class CoreTop extends Module {
  val io = IO(new Bundle {
    val iCache = new ICacheIO
    val dCache = new DCacheIO
    val debug  = new DebugIO
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

  io.iCache.sram_rdata <> fetch.iCache.inst_sram_rdata
  io.iCache.sram_en <> fetch.iCache.inst_sram_en
  io.iCache.sram_addr <> fetch.iCache.inst_sram_addr

  io.debug <> writeback.debug

  io.dCache <> memory.dCache

  fetch.jinfo <> decode.jinfo
  fetch.binfo <> execute.binfo

  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen <> decode.wb.wen

  io.iCache.sram_wen   := 0.U
  io.iCache.sram_wdata := 0.U
}
