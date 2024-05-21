package cpu.core.pipeline.top

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache  = new DCacheIO
    val dHazard = Output(new DataHazardMem)
    val ctrlreq = Output(new CtrlRequest)
    val exe     = new Bundle { val isMTC0 = Output(Bool()) }
    val ctrl    = Input(new CtrlInfo)
    val in      = Flipped(Decoupled((new StageExecuteMemory)))
    val out     = Decoupled(new StageMemoryWriteback)
  })

  val input  = io.in.bits
  val output = io.out.bits

  val memAccess = Module(new MemAccess).io

  memAccess.dCache  <> io.dCache
  memAccess.reqInfo <> input.memReqInfo
  memAccess.inst    <> input.inst
  memAccess.data    <> input.data
  memAccess.memByte <> input.memByte
  memAccess.ctrl    <> io.ctrl

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := memAccess.out

  io.exe.isMTC0 := input.inst.fu === fu_sp && input.inst.fuop === cp0_mtc0

  val except = WireDefault(input.exInfo)

  io.ctrlreq.block := io.dCache.stall
  io.ctrlreq.clear := false.B
  io.in.ready      := io.out.ready
  io.out.valid     := io.in.valid

  output.exInfo   := except
  output.slot     := input.slot
  output.exSel    := input.exSel
  output.data     := memAccess.out
  output.rsaddr   := input.rsaddr
  output.rtaddr   := input.rtaddr
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
}
