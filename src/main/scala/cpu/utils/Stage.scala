package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._

class Stage[T <: Data](dataType: T) extends Module {
  val io = IO(new Bundle {
    val in  = Input(dataType)
    val out = Output(dataType)
  })
  val reg = Reg(dataType)
  reg    := io.in
  io.out := reg
}
