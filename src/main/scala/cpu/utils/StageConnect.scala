package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config

object StageConnect {
  def stageConnect[T <: Bundle](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val fire = left.valid && right.ready
    right.bits <> RegEnable(left.bits, 0.U.asTypeOf(left.bits), fire)
    right.valid <> RegEnable(left.valid, true.B, fire)
    left.ready <> right.ready
    // left.ready <> RegEnable(right.ready, true.B, fire)
  }
}
