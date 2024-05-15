package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

class DataHazard extends Bundle {
  val wen   = Bool()
  val waddr = UInt(REG_WIDTH.W)
  val wdata = UInt(DATA_WIDTH.W)
}
class DataHazardMem extends DataHazard {}
class DataHazardExe extends DataHazard {
  val isload = Output(Bool())
}
