package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.core.pipeline.components.execute._
import cpu.core.pipeline.components.memory._

class MemCtrl extends Module with Config {
  val io = IO(new Bundle {
    val dCache = new DCacheIO
    val exe    = Flipped(new DCacheIOExe)
    val mem    = Flipped(new DCacheIOMem)
  })
  // io.exe         <> io.dCache
  // io.mem.data_ok <> io.dCache.data_ok
  // io.mem.rdata   <> io.dCache.rdata
}
