package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

class BraInfo extends Bundle {
  val bwen   = Bool()
  val bwaddr = UInt(PC_WIDTH.W)
}
class BraResult extends Bundle {
  val isb   = Bool()
  val bwen  = Bool()
  val index = UInt(BPU_INDEX_WIDTH.W)
}
