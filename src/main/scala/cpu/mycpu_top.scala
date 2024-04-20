package cpu

import chisel3._
import chisel3.util._

import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline._

class mycpu_top extends Module {
  val inst  = IO(new ICacheIO).suggestName("inst")
  val data  = IO(new DCacheIO).suggestName("data")
  val debug = IO(new DebugIO).suggestName("debug")

  // this.clock.suggestName("clk")
  // this.reset.suggestName("resetn")

  val core = Module(new CoreTop).io
  
  inst <> core.iCache
  data <> core.dCache
  debug <> core.debug
}
