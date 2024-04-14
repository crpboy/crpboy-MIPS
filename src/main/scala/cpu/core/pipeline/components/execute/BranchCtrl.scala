package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._

class JumpCtrl extends Module {
  val io = IO(new Bundle {
    val inst    = Input(new InstInfo)
    val wen = Output(Bool())
    val waddr = Output(UInt(PC_WIDTH.W))
  })
}
