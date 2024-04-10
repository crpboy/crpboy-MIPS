package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import cpu.common.Config._
import cpu.common.Const._

class MemDataIO extends Bundle {
  val en       = Input(Bool())
  val instType  = Input(UInt(INS_LEN.W))
  val dataAddr = Input(UInt(ADDR_WIDTH.W))
  val memData  = Output(UInt(INST_WIDTH.W))
}

class MemData extends Module {
  val io  = IO(new MemDataIO)
  val mem = Mem(MEM_DATA_SIZE, UInt(DATA_WIDTH.W))
  val reg = Reg(UInt(DATA_WIDTH.W))
  when(io.en && io.instType === inst_lw) { reg := mem.read(io.dataAddr) }
  io.memData := reg
}
