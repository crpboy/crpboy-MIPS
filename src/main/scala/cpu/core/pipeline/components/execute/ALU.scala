package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class ALUIO extends Bundle {
  val en       = Input(Bool())
  val op1Type  = Input(UInt(OP_LEN.W))
  val op2Type  = Input(UInt(OP_LEN.W))
  val instType = Input(UInt(INS_LEN.W))
  val aluType  = Input(UInt(ALU_LEN.W))
  val imm      = Input(UInt(IMM_WIDTH.W))
  val regData  = Input(new RegDataBundle)
  val aluRes   = Output(UInt(DATA_WIDTH.W))
}

class ALU extends Module {
  val io  = IO(new ALUIO)
  val reg = io.regData
  val op1 = MuxLookup(
    io.op1Type,
    0.U,
    Seq(
      op_reg -> reg.rs,
      op_imm -> io.imm,
    ),
  )
  val op2 = MuxLookup(
    io.op2Type,
    0.U,
    Seq(
      op_reg -> reg.rt,
      op_imm -> io.imm,
    ),
  )
  val res = MuxLookup(
    io.aluType,
    0.U,
    Seq(
      alu_or   -> (op1 | op2),
      alu_and  -> (op1 & op2),
      alu_xor  -> (op1 ^ op2),
      alu_nor  -> ~(op1 | op2),
      alu_sll  -> (op2 << op1(4, 0)),
      alu_srl  -> (op2 >> op1(4, 0)),
      alu_sra  -> (op2.asSInt() >> op1(4, 0)).asUInt,
      alu_add  -> (op1 + op2),
      alu_sub  -> (op1 - op2),
      alu_slt  -> (op1.asSInt() < op2.asSInt()),
      alu_sltu -> (op1 < op2),
      alu_clo  -> (getclo(op1)),
      alu_clz  -> (getclz(op1)),
    ),
  )
  io.aluRes := res
}
