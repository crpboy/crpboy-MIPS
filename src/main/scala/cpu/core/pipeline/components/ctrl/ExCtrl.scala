package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common.Const._

class ExCtrl extends Module {
  val io = IO(new Bundle {
    val exreq   = Input(UInt(CTRL_WIDTH.W))
    val invalid = Output(UInt(CTRL_WIDTH.W))
  })
  val res = WireDefault(0.U.asTypeOf(Vec(5, Bool())))
  for (i <- 0 until CTRL_WIDTH) {
    for (j <- i + 1 until CTRL_WIDTH) {
      res(i) := res(i) | io.exreq(j)
    }
  }
  io.invalid := res.asUInt
}
