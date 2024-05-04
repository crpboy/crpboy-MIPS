package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.StageConnect._

import components._
import components.cp0._
import components.ctrl._
import components.fetch._
import components.decode._
import components.execute._
import components.memory._
import components.writeback._

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

  // functional unit (control, cp0)
  val sfCtrlUnit = Module(new StallFlushCtrl)
  val exCtrlUnit = Module(new ExCtrl)
  val cp0Unit    = Module(new CP0)

  // unit io
  val fetch     = fetchUnit.io
  val decode    = decodeUnit.io
  val execute   = executeUnit.io
  val memory    = memoryUnit.io
  val writeback = writebackUnit.io
  val sfCtrl    = sfCtrlUnit.io
  val exCtrl    = exCtrlUnit.io
  val cp0       = cp0Unit.io

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
  sfCtrl.ifreq  <> fetch.ctrlreq
  sfCtrl.idreq  <> decode.ctrlreq
  sfCtrl.exereq <> execute.ctrlreq
  sfCtrl.memreq <> memory.ctrlreq
  sfCtrl.wbreq  <> writeback.ctrlreq
  sfCtrl.cp0req <> cp0.ctrlreq

  sfCtrl.stall(4) <> fetch.ctrl.stall
  sfCtrl.stall(3) <> decode.ctrl.stall
  sfCtrl.stall(2) <> execute.ctrl.stall
  sfCtrl.stall(1) <> memory.ctrl.stall
  sfCtrl.stall(0) <> writeback.ctrl.stall

  sfCtrl.flush(4) <> fetch.ctrl.flush
  sfCtrl.flush(3) <> decode.ctrl.flush
  sfCtrl.flush(2) <> execute.ctrl.flush
  sfCtrl.flush(1) <> memory.ctrl.flush
  sfCtrl.flush(0) <> writeback.ctrl.flush

  // exception ctrl
  exCtrl.exID  <> decode.out.exInfo
  exCtrl.exEXE <> execute.out.exInfo
  exCtrl.exMEM <> memory.out.exInfo
  exCtrl.exWB  <> cp0.exInfo

  exCtrl.out(4) <> fetch.ctrl.ex
  exCtrl.out(3) <> decode.ctrl.ex
  exCtrl.out(2) <> execute.ctrl.ex
  exCtrl.out(1) <> memory.ctrl.ex
  exCtrl.out(0) <> writeback.ctrl.ex

  // pipeline stage reg connect
  stageConnect(fetch.out,   decode.in,    decode.ctrl)
  stageConnect(decode.out,  execute.in,   execute.ctrl)
  stageConnect(execute.out, memory.in,    memory.ctrl)
  stageConnect(memory.out,  writeback.in, writeback.ctrl)

  // forward: -> fetch
  fetch.jinfo              <> decode.jinfo
  fetch.binfo              <> execute.binfo
  fetch.slotSignal.decode  <> decode.isSlot
  fetch.slotSignal.execute <> execute.isSlot

  // forward: data hazard
  execute.dHazard   <> decode.exeDHazard
  memory.dHazard    <> decode.memDHazard
  writeback.dHazard <> decode.wbDHazard

  // writeback: write regfile
  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen   <> decode.wb.wen

  // cp0 connect
  // exception connect (exe, wb) -> cp0 -> fetch
  writeback.cp0.exout <> cp0.wb
  writeback.cp0.exres <> cp0.exInfo
  writeback.cp0.wCp0  <> cp0.write
  cp0.fetch           <> fetch.cp0
  execute.rCp0        <> cp0.read
  cp0.extIntIn        := 0.U
  // slot judge (exe, mem) -> wb
  execute.out.slot      <> writeback.exe.slot
  execute.out.exInfo.en <> writeback.exe.ex
  memory.out.slot       <> writeback.mem.slot
  memory.out.exInfo.en  <> writeback.mem.ex
}
