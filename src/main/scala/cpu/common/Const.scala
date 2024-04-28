package cpu.common

import chisel3._

object Const extends ConstDecode with ConstWidth with Instructions {
  val PC_INIT_ADDR = "hbfc00000"

  val WB_EN = "b1111".U
  val WB_NO = "b0000".U
  val WI_EN = "b1111".U
  val WI_NO = "b0000".U
  val WD_EN = "b1111".U
  val WD_NO = "b0000".U
}
