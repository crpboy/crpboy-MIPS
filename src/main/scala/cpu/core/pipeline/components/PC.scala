package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._

class PCIO extends Bundle {
  val pc = Output(UInt(ADDR_WIDTH.W)) // current pc address
}

class PC extends Module {
  val io = IO(new PCIO)
  val pcReg = RegInit(START_ADDR.U(ADDR_WIDTH.W))
  pcReg := pcReg + ADDR_BYTE_WIDTH.U // pc += 4
  io.pc := pcReg
}
