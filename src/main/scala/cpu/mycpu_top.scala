package cpu

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.core._

class mycpu_top extends Module {
  val io = IO(new Bundle {
    val debug = new DebugIO
  })
  val core = Module(new CoreTop).io
  io.debug <> core.debug
}
