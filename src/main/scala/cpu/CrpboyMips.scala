package cpu

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.config.Config._
import cpu.core.PCreg

class CrpboyMipsIO extends Bundle {
  val addr = Input(UInt(ADDR_WIDTH.W))
  val inst = Input(UInt(INST_WIDTH.W))
}

class CrpboyMips extends Module {
  val io = IO(new CrpboyMipsIO())
}
