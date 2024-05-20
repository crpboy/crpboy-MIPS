package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.StageConnect._

import top._
import components._
import components.cp0._
import components.ctrl._

class CoreTop extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(INT_WIDTH.W))
    val iCache  = new ICacheIO
    val dCache  = new DCacheIO
    val debug   = new DebugIO
  })

  // pipeline unit
  val fetch     = Module(new FetchUnit).io
  val decode    = Module(new DecodeUnit).io
  val execute   = Module(new ExecuteUnit).io
  val memory    = Module(new MemoryUnit).io
  val writeback = Module(new WriteBackUnit).io

  // functional unit
  val sfCtrl = Module(new StallFlushCtrl).io
  val exCtrl = Module(new ExCtrl).io
  val cp0    = Module(new CP0).io

  // sram connect
  io.debug  <> writeback.debug
  io.iCache <> fetch.iCache
  io.dCache <> memory.dCache

  // stall and flush control
  sfCtrl.ifreq       <> fetch.ctrlreq
  sfCtrl.idreq       <> decode.ctrlreq
  sfCtrl.exereq      <> execute.ctrlreq
  sfCtrl.memreq      <> memory.ctrlreq
  sfCtrl.wbreq       <> writeback.ctrlreq
  sfCtrl.dCacheStall <> io.dCache.stall

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

  // cache stall ctrl
  io.iCache.stall <> fetch.ctrl.cache.iStall
  io.iCache.stall <> decode.ctrl.cache.iStall
  io.iCache.stall <> execute.ctrl.cache.iStall
  io.iCache.stall <> memory.ctrl.cache.iStall
  io.iCache.stall <> writeback.ctrl.cache.iStall

  io.dCache.stall <> fetch.ctrl.cache.dStall
  io.dCache.stall <> decode.ctrl.cache.dStall
  io.dCache.stall <> execute.ctrl.cache.dStall
  io.dCache.stall <> memory.ctrl.cache.dStall
  io.dCache.stall <> writeback.ctrl.cache.dStall

  io.iCache.stall <> cp0.stall // temp

  // exception ctrl
  exCtrl.exID  <> decode.out.bits.exInfo
  exCtrl.exEXE <> execute.out.bits.exInfo
  exCtrl.exMEM <> memory.out.bits.exInfo
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

  // forward: to fetch
  fetch.jinfo              <> decode.jinfo
  fetch.binfo              <> execute.binfo
  fetch.slotSignal.decode  <> decode.fetch.isJmp
  fetch.slotSignal.execute <> execute.fetch.isBr

  // forward: data hazard
  execute.dHazard   <> decode.exeDHazard
  memory.dHazard    <> decode.memDHazard
  writeback.dHazard <> decode.wbDHazard

  // writeback: write regfile
  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen   <> decode.wb.wen

  // ex: exception connect (exe, wb) -> cp0 -> fetch
  writeback.cp0.exout <> cp0.wb
  writeback.cp0.exres <> cp0.exInfo
  writeback.cp0.wCp0  <> cp0.write
  cp0.fetch           <> fetch.cp0
  execute.rCp0        <> cp0.read
  cp0.extIntIn        <> io.ext_int
  execute.memory      <> memory.execute

  // ex: slot judge (exe, mem) -> wb
  execute.out.bits.slot      <> writeback.exe.slot
  execute.out.bits.exInfo.en <> writeback.exe.ex
  memory.out.bits.slot       <> writeback.mem.slot
  memory.out.bits.exInfo.en  <> writeback.mem.ex
}
