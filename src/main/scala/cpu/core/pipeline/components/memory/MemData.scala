package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.common.Const._

class MemData extends Module {
  val io = IO(new Bundle {
    val en       = Input(Bool())
    val instType = Input(UInt(FU_LEN.W))
    val dataAddr = Input(UInt(ADDR_WIDTH.W))
    val memData  = Output(UInt(INST_WIDTH.W))
  })
  val mem = Mem(MEM_DATA_SIZE, UInt(DATA_WIDTH.W))
  val reg = Reg(UInt(DATA_WIDTH.W))
  // when(io.en && io.instType === inst_lw) { reg := mem.read(io.dataAddr) }
  io.memData := reg
}
