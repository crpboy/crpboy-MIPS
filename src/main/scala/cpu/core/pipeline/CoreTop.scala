package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.utils.StageConnect._
import cpu.common._
import cpu.common.Const._

import cpu.core.pipeline.components._
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

  // pipeline unit
  val fetchUnit     = Module(new FetchUnit)
  val decodeUnit    = Module(new DecodeUnit)
  val executeUnit   = Module(new ExecuteUnit)
  val memoryUnit    = Module(new MemoryUnit)
  val writebackUnit = Module(new WriteBackUnit)

  // control unit
  val keepFlushCtrlUnit = Module(new KeepFlushCtrl)

  // unit io
  val fetch         = fetchUnit.io
  val decode        = decodeUnit.io
  val execute       = executeUnit.io
  val memory        = memoryUnit.io
  val writeback     = writebackUnit.io
  val keepFlushCtrl = keepFlushCtrlUnit.io

  // pipeline stage reg connect
  stageConnect(fetch.out,   decode.in)
  stageConnect(decode.out,  execute.in)
  stageConnect(execute.out, memory.in)
  stageConnect(memory.out,  writeback.in)

  // top io
  io.iCache.sram_rdata <> fetch.iCache.inst_sram_rdata
  io.iCache.sram_en    <> fetch.iCache.inst_sram_en
  io.iCache.sram_addr  <> fetch.iCache.inst_sram_addr
  io.iCache.sram_wen   := 0.U
  io.iCache.sram_wdata := 0.U

  io.dCache.sram_rdata <> memory.dCache.sram_rdata
  io.dCache.sram_en    <> execute.dCache.sram_en
  io.dCache.sram_addr  <> execute.dCache.sram_addr
  io.dCache.sram_wen   <> execute.dCache.sram_wen
  io.dCache.sram_wdata <> execute.dCache.sram_wdata

  io.debug <> writeback.debug

  // keep and flush control
  keepFlushCtrl.ifreq  <> fetch.ctrlreq
  keepFlushCtrl.idreq  <> decode.ctrlreq
  keepFlushCtrl.exereq <> execute.ctrlreq
  keepFlushCtrl.memreq <> memory.ctrlreq
  keepFlushCtrl.wbreq  <> writeback.ctrlreq

  keepFlushCtrl.keep(4) <> fetch.ctrl.keep
  keepFlushCtrl.keep(3) <> decode.in.ctrl.keep
  keepFlushCtrl.keep(2) <> execute.in.ctrl.keep
  keepFlushCtrl.keep(1) <> memory.in.ctrl.keep
  keepFlushCtrl.keep(0) <> writeback.in.ctrl.keep

  keepFlushCtrl.flush(4) <> fetch.ctrl.flush
  keepFlushCtrl.flush(3) <> decode.in.ctrl.flush
  keepFlushCtrl.flush(2) <> execute.in.ctrl.flush
  keepFlushCtrl.flush(1) <> memory.in.ctrl.flush
  keepFlushCtrl.flush(0) <> writeback.in.ctrl.flush

  // jump and branch info
  fetch.jinfo <> decode.jinfo
  fetch.binfo <> execute.binfo

  // data hazard
  execute.dHazard   <> decode.exeDHazard
  memory.dHazard    <> decode.memDHazard
  writeback.dHazard <> decode.wbDHazard

  // writeback
  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen   <> decode.wb.wen
}
