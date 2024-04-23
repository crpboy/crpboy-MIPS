package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config
import cpu.common._

object StageConnect {
  def stageConnect[T <: Data](left: T, right: KeepFlushIO[T]) = {
    val zero = 0.U.asTypeOf(left)
    val reg  = RegInit(zero)
    reg := MuxCase(
      left,
      Seq(
        right.ctrl.keep  -> right.bits,
        right.ctrl.flush -> zero,
      ),
    )
    right.bits <> reg
  }
}
