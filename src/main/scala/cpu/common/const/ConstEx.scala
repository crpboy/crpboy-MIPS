package cpu.common.const

import chisel3._
import chisel3.util._

trait ConstEx {
  val EX_LEN  = 5
  val ex_Int  = "h00".U
  val ex_Mod  = "h01".U
  val ex_TLBL = "h02".U
  val ex_TLBS = "h03".U
  val ex_AdEL = "h04".U
  val ex_AdES = "h05".U
  val ex_Sys  = "h08".U
  val ex_Bp   = "h09".U
  val ex_RI   = "h0a".U
  val ex_Ov   = "h0c".U
}
