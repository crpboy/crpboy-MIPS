package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache  = new DCacheIOMem
    val dHazard = Output(new DataHazardMem)
    val ctrlreq = Output(new CtrlRequest)
    val ctrl    = Input(new CtrlInfo)
    val in      = Flipped(Decoupled((new StageExecuteMemory)))
    val out     = Decoupled(new StageMemoryWriteback)
  })

  val input  = io.in.bits
  val output = io.out.bits

  val load = Module(new LoadAccess).io

  load.dCache  <> io.dCache
  load.inst    <> input.inst
  load.data    <> input.data
  load.memByte <> input.memByte
  load.ctrl    <> io.ctrl

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := load.out

  val except = WireDefault(input.exInfo)

  io.ctrlreq.block := load.block
  io.ctrlreq.clear := false.B
  io.in.ready      := io.out.ready
  io.out.valid     := io.in.valid

  output.exInfo   := except
  output.slot     := input.slot
  output.exSel    := input.exSel
  output.data     := load.out
  output.rsaddr   := input.rsaddr
  output.rtaddr   := input.rtaddr
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
}
