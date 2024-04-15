package cpu

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.core._

class mycpu_top extends Module {
  val io = IO(new Bundle {
    val debug  = Output(new DebugInfo)
    // val iCache = new ICacheIO
    // val dCache = new DCacheIO
  })
  val core = Module(new CoreTop).io
  io.debug <> core.debug
  // io.iCache <> core.iCache
  // io.iCache <> core.dCache
}
