package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.writeback._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new StageMemoryWriteback))
    val out = Output(new WBInfo)

    val debug = new DebugIO
  })
  val input = io.in.bits

  io.out.wen   := input.inst.wb
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  io.debug.wb_pc       := input.debug_pc
  io.debug.wb_rf_wdata := input.data
  io.debug.wb_rf_wen   := Mux(input.inst.wb, WB_EN, WB_NO)
  io.debug.wb_rf_wnum  := input.inst.rd

  io.in.ready := true.B
}
