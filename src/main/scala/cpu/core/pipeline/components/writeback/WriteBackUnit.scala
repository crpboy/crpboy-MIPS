package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.core.pipeline.components.writeback._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(Input(new StageMemoryWriteback)))
    val out = Output(new WBInfo)
  })
  val input  = io.in.bits
  val output = io.out
  output.wen   := input.inst.wb
  output.wdata := input.data
  output.waddr := input.inst.rd
  io.in.ready  := true.B
}
