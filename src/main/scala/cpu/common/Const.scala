package cpu.common

import chisel3._

object Const extends ConstDecode with ConstWidth with Instructions {
  val PC_INIT_ADDR     = "hbfc00000"
  val PC_INIT_ADDR_SUB = "hbfbffffc"
  val EX_INIT_ADDR     = "hbfc00380"

  val WB_EN = "b1111".U
  val WB_NO = "b0000".U
}
