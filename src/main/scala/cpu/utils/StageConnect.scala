package cpu.utils

import chisel3._
import chisel3.util._

object StageConnect {
  def stageConnect[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    right.bits <> RegEnable(left.bits, left.fire)
    right.valid <> left.valid
    right.ready <> left.ready
  }
}
