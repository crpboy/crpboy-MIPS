package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class PreFetch extends Module {
  val io = IO(new Bundle {
    val iCache = new ICacheIO
    val in     = Flipped(Decoupled((UInt(PC_WIDTH.W))))
    val out    = Decoupled(UInt(INST_WIDTH.W))
  })
}
