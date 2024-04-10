package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._

class PCIO extends Bundle {
  val en       = Input(Bool())
  val pc       = Output(UInt(ADDR_WIDTH.W))
}

class PC extends Module {
  val io    = IO(new PCIO)
  val pcReg = RegInit(PC_START_ADDR.U(ADDR_INST_WIDTH.W))
  when(io.en) { pcReg := pcReg + INST_BYTE_WIDTH.U }
  io.pc := pcReg
}
