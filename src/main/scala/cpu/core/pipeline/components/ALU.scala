package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class ALUIO extends Bundle {
  val instOp1Type = Input(Bool())              // decoder
  val instOp2Type = Input(Bool())              // decoder
  val instAluType = Input(UInt(alu_len.W))     // decoder
  val imm         = Input(UInt(DATA_WIDTH.W))  // decoder
  val regData     = Input(new BundleRegData)   // register
  val aluRes      = Output(UInt(DATA_WIDTH.W)) // -> reg & mem
}

class ALU extends Module {
  val io = IO(new ALUIO)
  val op1 = MuxLookup(
    io.instOp1Type,
    0.U,
    Seq(
      op_reg -> io.regData.rs,
      op_imm -> io.imm,
    ),
  )
  val op2 = MuxLookup(
    io.instOp2Type,
    0.U,
    Seq(
      op_reg -> io.regData.rt,
      op_imm -> io.imm,
    ),
  )
  val res = MuxLookup(
    io.instAluType,
    0.U,
    Seq(
      alu_x    -> 0.U,
      alu_or   -> (op1 | op2),
      alu_and  -> (op1 & op2),
      alu_xor  -> (op1 ^ op2),
      alu_nor  -> ~(op1 | op2),
      alu_sll  -> (op2 << op1(4, 0)),
      alu_srl  -> (op2 >> op1(4, 0)),
      alu_sra  -> (op2.asSInt() >> op1(4, 0)).asUInt,
      alu_add  -> (op1 + op2),
      alu_addu -> (op1 + op2),
      alu_sub  -> (op1 - op2),
      alu_subu -> (op1 - op2),
      alu_mul  -> (op1 * op2),
      alu_div  -> (op1 / op2),
      alu_slt  -> (op1.asSInt() < op2.asSInt()),
      alu_sltu -> (op1 < op2),
      alu_clo  -> (getclo(op1)),
      alu_clz  -> (getclz(op1)),
    ),
  )
  io.aluRes := res
}
