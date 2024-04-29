package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._

import cpu.utils.Functions._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache  = new Bundle { val sram_rdata = Input(UInt(DATA_WIDTH.W)) }
    val dHazard = Output(new DataHazard)
    val in      = new StallFlushIO(new StageExecuteMemory)
    val out     = new StageMemoryWriteback
    val ctrlreq = Output(new CtrlRequest)
  })

  val input  = io.in.bits
  val output = io.out

  val load = Module(new LoadAccess).io

  load.dCache  <> io.dCache
  load.inst    <> input.inst
  load.data    <> input.data
  load.memByte <> input.memByte

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := load.out

  io.ctrlreq.block := false.B
  io.ctrlreq.clear := false.B

  output.data     := load.out
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
}
