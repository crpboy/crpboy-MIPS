package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class ALUIO extends Bundle {
  val opt    = Input(UInt(OP_WIDTH.W))
  val useImm = Input(Bool())
  val imm    = Input(UInt(IMM_WIDTH.W))
  val data   = Input(new BundleRegData)
  val res    = Output(UInt(DATA_WIDTH.W)) // to reg & mem
}

class ALU extends Module {
  val io  = IO(new ALUIO)
  val op1 = io.data.rs
  val op2 = Mux(io.useImm, io.imm, io.data.rt)
  val clo = WireInit(32.U)
  val clz = WireInit(32.U)
  for (i <- 0 until 32) {
    when(!op1(i)) {
      clo := (31 - i).U
    }.otherwise {
      clz := (31 - i).U
    }
  }
  io.res := MuxLookup(
    io.opt,
    0.U,
    Seq(
      OP_N     -> 0.U,
      ALU_OR   -> (op1 | op2),
      ALU_AND  -> (op1 & op2),
      ALU_XOR  -> (op1 ^ op2),
      ALU_NOR  -> ~(op1 | op2),
      ALU_SLL  -> (op2 << op1(4, 0)),
      ALU_SRL  -> (op2 >> op1(4, 0)),
      ALU_SRA  -> (op2.asSInt() >> op1(4, 0)).asUInt,
      ALU_SLT  -> (op1.asSInt() < op2.asSInt()),
      ALU_SLTU -> (op1 < op2),
      ALU_ADD  -> (op1 + op2),
      ALU_ADDU -> (op1 + op2),
      ALU_SUB  -> (op1 - op2),
      ALU_SUBU -> (op1 - op2),
      ALU_MUL  -> (op1 * op2),
      ALU_CLO  -> (clo),
      ALU_CLZ  -> (clz),
    ),
  )
}
