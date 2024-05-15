package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

class InstInfo extends Bundle {
  val wb   = Bool()
  val fu   = UInt(FU_LEN.W)
  val fuop = UInt(FUOP_LEN.W)
  val rd   = UInt(REG_WIDTH.W)
}
class InstInfoExt extends InstInfo {
  val op1 = UInt(OPR_LEN.W)
  val op2 = UInt(OPR_LEN.W)
  val imm = UInt(DATA_WIDTH.W)
}