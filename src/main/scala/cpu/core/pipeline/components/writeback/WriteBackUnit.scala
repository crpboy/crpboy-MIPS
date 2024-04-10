package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class WriteBackUnitIO extends Bundle {
  val in  = Input(new StageMemoryWriteback)
  val out = Output(new WBInfo)
}

class WriteBackUnit extends Module {
  val io = IO(new WriteBackUnitIO)
  io.out.rd   := io.in.instInfo.rd
  io.out.wb   := io.in.instInfo.wb
  io.out.data := io.in.data
}
