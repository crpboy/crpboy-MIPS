package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common._

object StageConnect {
  def stageConnect[T <: PipelineStage](left: T, right: T, ctrl: CtrlInfo) = {
    val zero = 0.U.asTypeOf(left)
    val reg  = RegInit(zero)

    val stallSignal = ctrl.stall
    val flushSignal = ctrl.flush || ctrl.ex

    reg := MuxCase(
      left,
      Seq(
        stallSignal -> right,
        flushSignal -> zero,
      ),
    )
    right <> reg
  }
}
