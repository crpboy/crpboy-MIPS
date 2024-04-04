package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

import cpu.common.Config._

class MemDataIO extends Bundle {
  val out = Output(UInt(INST_WIDTH.W))
}

class MemData extends Module {
  val io  = IO(new MemDataIO)
  val mem = Mem(MEM_DATA_SIZE, UInt(DATA_WIDTH.W))
}
