package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

// write back info
class JWBInfo extends Bundle {
  val wen   = Bool()
  val wdata = UInt(DATA_WIDTH.W)
}
class WBInfo extends Bundle {
  val wen   = Bool()
  val wdata = UInt(DATA_WIDTH.W)
  val waddr = UInt(REG_WIDTH.W)
}

// jump info
class JmpInfo extends Bundle {
  val jwen   = Bool()
  val jwaddr = UInt(PC_WIDTH.W)
}