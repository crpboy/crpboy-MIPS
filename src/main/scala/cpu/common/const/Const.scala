package cpu.common.const

import chisel3._

object Const extends ConstDecode with ConstWidth with ConstEx with Instructions with ConstWidthAxi with ConstWidthTlb {
  val PC_INIT_ADDR     = "hbfc00000"
  val PC_INIT_ADDR_SUB = "hbfbffffc"

  val EX_INIT_ADDR       = "hbfc00380".U
  val EX_TLB_REFILL_ADDR = "hbfc00200".U

  val KUSEG_START_ADDR  = "h0000_0000".U // mapped, for user
  val KSEG0_START_ADDR  = "h8000_0000".U // unmapped, cached
  val KSEG1_START_ADDR  = "hA000_0000".U // unmapped, uncached
  val KSEG23_START_ADDR = "hC000_0000".U // mapped

  val WB_EN = "b1111".U
  val WB_NO = "b0000".U
}
