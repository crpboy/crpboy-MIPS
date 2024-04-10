package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.core.pipeline.components.writeback._
import cpu.utils._

class WriteBackTopIO extends Bundle {
  val in  = Input(new StageMemoryWriteback)
  val out = Output(new WBInfo)
}

class WriteBackTop extends Module {
  val io    = IO(new WriteBackTopIO)
  val stage = Module(new Stage(new StageMemoryWriteback)).io
  val wb    = Module(new WriteBackUnit).io
  stage.in <> io.in
  stage.out <> wb.in
  wb.out <> io.out
}
