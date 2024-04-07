package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class RegisterIO extends Bundle {
  // decoder
  val regAddr      = Input(new BundleReg)
  val instOp1Type  = Input(UInt(op_len.W))
  val instOp2Type  = Input(UInt(op_len.W))
  val instSrcType  = Input(UInt(src_len.W))
  val aluRes       = Input(UInt(DATA_WIDTH.W))
  val memDataRes   = Input(UInt(DATA_WIDTH.W))
  val regData      = Output(new BundleRegData)
}
class Register extends Module {
  val io  = IO(new RegisterIO)
  val reg = Reg(Vec(REG_NUMS, UInt(DATA_WIDTH.W)))
  io.regData.rs := Mux(io.instOp1Type === op_reg, reg(io.regAddr.rs), 0.U)
  io.regData.rt := Mux(io.instOp2Type === op_reg, reg(io.regAddr.rt), 0.U)
  when(io.instSrcType =/= src_x) {
    reg(io.regAddr.rd) := MuxLookup(
      io.instSrcType,
      reg(io.regAddr.rd),
      Seq(
        src_alu -> io.aluRes,
        src_mem -> io.memDataRes,
      ),
    )
  }
}
