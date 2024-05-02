package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.StageConnect._

import cpu.core.pipeline.components._
import cpu.core.pipeline.components.ctrl._
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
  val stallFlushCtrlUnit = Module(new StallFlushCtrl)
  // val exCtrlUnit         = Module(new ExCtrl)

  // unit io
  val fetch          = fetchUnit.io
  val decode         = decodeUnit.io
  val execute        = executeUnit.io
  val memory         = memoryUnit.io
  val writeback      = writebackUnit.io
  val stallFlushCtrl = stallFlushCtrlUnit.io
  // val exCtrl         = exCtrlUnit.io

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

  // stall and flush control
  stallFlushCtrl.ifreq  <> fetch.ctrlreq
  stallFlushCtrl.idreq  <> decode.ctrlreq
  stallFlushCtrl.exereq <> execute.ctrlreq
  stallFlushCtrl.memreq <> memory.ctrlreq
  stallFlushCtrl.wbreq  <> writeback.ctrlreq

  stallFlushCtrl.stall(4) <> fetch.ctrl.stall
  stallFlushCtrl.stall(3) <> decode.ctrl.stall
  stallFlushCtrl.stall(2) <> execute.ctrl.stall
  stallFlushCtrl.stall(1) <> memory.ctrl.stall
  stallFlushCtrl.stall(0) <> writeback.ctrl.stall

  stallFlushCtrl.flush(4) <> fetch.ctrl.flush
  stallFlushCtrl.flush(3) <> decode.ctrl.flush
  stallFlushCtrl.flush(2) <> execute.ctrl.flush
  stallFlushCtrl.flush(1) <> memory.ctrl.flush
  stallFlushCtrl.flush(0) <> writeback.ctrl.flush

  // pipeline stage reg connect
  stageConnect(fetch.out,   decode.in,    decode.ctrl)
  stageConnect(decode.out,  execute.in,   execute.ctrl)
  stageConnect(execute.out, memory.in,    memory.ctrl)
  stageConnect(memory.out,  writeback.in, writeback.ctrl)

  // jump and branch info
  fetch.jinfo  <> decode.jinfo
  fetch.binfo  <> execute.binfo
  fetch.exinfo := DontCare

  // data hazard
  execute.dHazard   <> decode.exeDHazard
  memory.dHazard    <> decode.memDHazard
  writeback.dHazard <> decode.wbDHazard

  // writeback
  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen   <> decode.wb.wen
}
