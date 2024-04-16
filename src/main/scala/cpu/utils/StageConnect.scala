package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config

object StageConnect {
  def stageConnect[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    if(Config.isPipeline) {
      right.bits <> RegEnable(left.bits, left.fire)
    }else{
      right.bits <> left.bits
    }
    right.valid <> left.valid
    right.ready <> left.ready
  }
}
