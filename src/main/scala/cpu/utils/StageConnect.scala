package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common._

object StageConnect {
  def stageConnect[T <: PipelineStage](predata: T, mydata: T, ctrl: CtrlInfo) = {
    val rawZero = 0.U.asTypeOf(predata)
    val reg     = RegInit(rawZero)

    val zero = WireDefault(rawZero)
    zero.debug_pc := mydata.debug_pc
    zero.pc       := mydata.pc
    zero.slot     := mydata.slot

    val stallSignal = ctrl.stall
    val flushSignal = ctrl.flush || ctrl.ex

    reg := MuxCase(
      predata,
      Seq(
        stallSignal -> mydata,
        flushSignal -> zero,
      ),
    )
    mydata <> reg
  }
}
