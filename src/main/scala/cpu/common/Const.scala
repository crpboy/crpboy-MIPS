package cpu.common

import cpu.common._
import chisel3._

object Const extends ConstDecode with ConstWidth with Instructions {
  val PC_INIT_ADDR = "hbfc00000"
  val nop_data     = 0.U(INST_WIDTH.W)
}
