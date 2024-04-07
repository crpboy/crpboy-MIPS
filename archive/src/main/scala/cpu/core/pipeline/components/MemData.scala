package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import cpu.common.Config._
import cpu.common.Const._

class MemDataIO extends Bundle {
  val srcType     = Input(UInt(DECODE_SRC_WIDTH.W)) // decoder
  val memDataAddr = Input(UInt(ADDR_WIDTH.W)) // alu
  val memDataRes  = Output(UInt(INST_WIDTH.W)) // -> reg
}

class MemData extends Module {
  val io  = IO(new MemDataIO)
  val mem = Mem(MEM_DATA_SIZE, UInt(DATA_WIDTH.W))
  val res = Mux(io.srcType === INST_MEM, mem.read(io.memDataAddr), 0.U)
  io.memDataRes := res
}
