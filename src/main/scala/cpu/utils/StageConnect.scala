package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

object StageConnect {
  def stageConnect[T <: PipelineStage](
      left: DecoupledIO[T],
      right: DecoupledIO[T],
      ctrl: CtrlInfo,
  ) = {
    val predata = left.bits
    val mydata  = right.bits

    val rawZero = 0.U.asTypeOf(predata)
    val reg     = RegInit(rawZero)

    val zero = WireDefault(rawZero)
    zero.debug_pc := mydata.debug_pc
    zero.pc       := mydata.pc
    zero.slot     := mydata.slot

    val stallSignal = ctrl.stall
    val flushSignal = ctrl.flush || ctrl.ex

    when(right.fire) {
      reg := MuxCase(
        predata,
        Seq(
          stallSignal -> mydata,
          flushSignal -> zero,
        ),
      )
    }
    mydata     := reg
    left.ready <> right.ready
    left.valid <> right.valid
  }
}
