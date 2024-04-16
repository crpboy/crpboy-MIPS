package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.core.pipeline.components.writeback._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val in    = Flipped(Decoupled(Input(new StageMemoryWriteback)))
    val out   = Output(new WBInfo)
    val debug = Output(new DebugInfo)
  })
  val input = io.in.bits

  io.out.wen   := input.inst.wb
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  io.debug.wb_pc       := input.debug_wb_pc
  io.debug.wb_rd_wdata := input.data
  io.debug.wb_rf_wen   := input.inst.wb
  io.debug.wb_rf_wnum  := input.inst.rd

  io.in.ready := true.B
}
