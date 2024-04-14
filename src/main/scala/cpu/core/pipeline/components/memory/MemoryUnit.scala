package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(Input(new StageExecuteMemory)))
    val out = Decoupled(Output(new StageMemoryWriteback))
  })
  val input  = io.in.bits
  val output = io.out.bits

  input <> output

  io.in.ready  := true.B
  io.out.valid := true.B
}
