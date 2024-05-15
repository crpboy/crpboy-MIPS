package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

class CtrlInfo extends Bundle {
  val stall = Bool()
  val flush = Bool()
  val ex    = Bool()
}
class CtrlRequest extends Bundle {
  val block = Bool()
  val clear = Bool()
}
class CtrlRequestExecute extends CtrlRequest {
  val branchPause = Bool()
}