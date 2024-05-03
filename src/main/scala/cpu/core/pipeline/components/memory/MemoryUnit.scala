package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._

import cpu.utils.Functions._
import cpu.common._
import cpu.common.Const._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache  = new Bundle { val sram_rdata = Input(UInt(DATA_WIDTH.W)) }
    val dHazard = Output(new DataHazard)
    val ctrlreq = Output(new CtrlRequest)

    val ctrl = Input(new CtrlInfo)
    val in   = Input(new StageExecuteMemory)
    val out  = Output(new StageMemoryWriteback)
  })

  val input  = io.in
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

  val except = WireDefault(input.exInfo)
  output.exInfo   := except
  output.data     := load.out
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
}
