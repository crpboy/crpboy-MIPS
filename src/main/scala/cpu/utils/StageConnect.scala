package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config

object StageConnect {
  def stageConnect[T <: Bundle](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val fire = left.valid && right.ready

    val zero = 0.U.asTypeOf(left.bits)
    val reg  = RegInit(zero)
    reg := Mux(fire, left.bits, zero)
    right.bits <> reg
    // right.bits <> RegEnable(left.bits, zero, fire)

    right.valid <> RegEnable(left.valid, true.B, fire)
    // right.valid <> left.valid

    left.ready <> right.ready
    // left.ready <> RegEnable(right.ready, true.B, fire)

  }
}
