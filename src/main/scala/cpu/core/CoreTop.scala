package cpu.core

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.core.pipeline._
import cpu.utils._

class CoreTopIO extends Bundle {
  val debugInfo = Input(new DebugInfo)
}

class CoreTop extends Module {
  val io        = IO(new CoreTopIO)
  val fetch     = Module(new FetchTop).io
  val decode    = Module(new DecodeTop).io
  val execute   = Module(new ExecuteTop).io
  val memory    = Module(new MemoryTop).io
  val writeback = Module(new WriteBackTop).io
  
  io.debugInfo.en <> fetch.in.en
  fetch.out <> decode.in
  decode.out <> execute.in
  execute.out <> memory.in
  memory.out <> writeback.in

  writeback.out <> execute.wbInfo
}
