package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import cpu.common.Config._
import cpu.utils._

class MemInstIO extends Bundle {
  val en   = Input(Bool())
  val pc   = Input(UInt(ADDR_WIDTH.W))
  val inst = Output(UInt(INST_WIDTH.W))
}

class MemInst extends Module {
  val io  = IO(new MemInstIO)
  val mem = Mem(MEM_INST_SIZE, UInt(INST_WIDTH.W))
  loadMemoryFromFile(mem, INST_HOME, MemoryLoadFileType.Hex)
  val reg = Reg(UInt(INST_WIDTH.W))
  when(io.en) { reg := mem.read(io.pc >> INST_BYTE_WIDTH_LOG.U) }
  io.inst := reg
}
