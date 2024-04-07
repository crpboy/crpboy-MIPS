package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class RegisterIO extends Bundle {
  val regAddr    = Input(new BundleReg)            // decoder
  val writeReg   = Input(Bool())                   // decoder
  val srcType    = Input(UInt(DECODE_SRC_WIDTH.W)) // decoder
  val aluRes     = Input(UInt(DATA_WIDTH.W))       // alu
  val memDataRes = Input(UInt(DATA_WIDTH.W))       // mem
  val regData    = Output(new BundleRegData)       // -> alu & mem
}
class Register extends Module {
  val io  = IO(new RegisterIO)
  val reg = Reg(Vec(REG_NUMS, UInt(DATA_WIDTH.W)))
  io.regData.rs := reg(io.regAddr.rs)
  io.regData.rt := reg(io.regAddr.rt)
  when(io.writeReg) {
    reg(io.regAddr.rd) := MuxLookup(
      io.srcType,
      reg(io.regAddr.rd),
      Seq(
        INST_N   -> reg(io.regAddr.rd),
        INST_ALU -> io.aluRes,
        INST_MEM -> io.memDataRes,
      ),
    )
  }
}
