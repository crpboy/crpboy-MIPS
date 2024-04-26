package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.writeback._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val dHazard = Output(new DataHazard)
    val in      = new KeepFlushIO(new StageMemoryWriteback)
    val out     = Output(new WBInfo)
    val ctrlreq = Output(new CtrlRequest)
    val debug   = new DebugIO
  })
  val input   = io.in.bits

  io.out.wen   := input.inst.wb
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := input.data

  io.ctrlreq.block := false.B
  io.ctrlreq.clear := false.B

  io.debug.wb_pc       := input.debug_pc
  io.debug.wb_rf_wdata := input.data
  io.debug.wb_rf_wen   := Mux(input.inst.wb, WB_EN, WB_NO)
  io.debug.wb_rf_wnum  := input.inst.rd
}
