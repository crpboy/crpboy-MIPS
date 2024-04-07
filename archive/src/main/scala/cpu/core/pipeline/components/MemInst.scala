package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import cpu.common.Config._

class MemInstIO extends Bundle {
  val pc   = Input(UInt(ADDR_WIDTH.W))  // pc address
  val inst = Output(UInt(INST_WIDTH.W)) // instruction
}

class MemInst extends Module {
  val io  = IO(new MemInstIO)
  val mem = Mem(MEM_INST_SIZE, UInt(INST_WIDTH.W))
  loadMemoryFromFile(mem, INST_HOME, MemoryLoadFileType.Hex)
  io.inst := mem.read(io.pc >> INST_BYTE_WIDTH_LOG.U)
}
