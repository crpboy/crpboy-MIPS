package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class RegisterIO extends Bundle {
  val regAddr = Input(new BundleReg)
  val writeReg = Input(Bool())
  val aluRes = Input(UInt(DATA_WIDTH.W))
  val memRes = Input(UInt(DATA_WIDTH.W))
  val data = Output(new BundleRegData)
}
class Register extends Module {
  val io = IO(new RegisterIO)
  val reg = Reg(Vec(REG_NUMS, UInt(DATA_WIDTH.W)))
  when(io.writeReg){
    // reg(io.regAddr.rd) := 
  }
}
