package cpu.core.pipeline.fetch

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

import cpu.config.Config._

class MemInstIO extends Bundle {
  // INPUT
  val addrPC = Input(UInt(ADDR_WIDTH.W)) // pc address
  // OUTPUT
  val inst = Output(UInt(INST_WIDTH.W)) // instruction
}

class MemInst extends Module {
  val io = IO(new MemInstIO())

  // create a memory which can store MEM_INST_SIZE instructions
  val mem = Mem(MEM_INST_SIZE, UInt(INST_WIDTH.W))

  loadMemoryFromFile(mem, INST_HOME, MemoryLoadFileType.Hex)
  io.inst := mem.read(io.addrPC >> INST_BYTE_WIDTH_LOG.U)
}
