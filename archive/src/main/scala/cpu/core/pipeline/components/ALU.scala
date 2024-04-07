package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class ALUIO extends Bundle {
  val op1Type  = Input(Bool())                    // decoder
  val op2Type  = Input(Bool())                    // decoder
  val instType = Input(UInt(DECODE_INST_WIDTH.W)) // decoder
  val imm      = Input(UInt(IMM_WIDTH.W))         // decoder
  val shamt    = Input(UInt(SHAMT_WIDTH.W))       // decoder
  val regData  = Input(new BundleRegData)         // register
  val aluRes   = Output(UInt(DATA_WIDTH.W))       // -> reg & mem
}

class ALU extends Module {
  val io = IO(new ALUIO)
  val op1 = MuxLookup(
    io.op1Type,
    0.U,
    Seq(
      OPn_RF  -> io.regData.rs,
      OPn_IMM -> io.imm,
    ),
  )
  val op2 = MuxLookup(
    io.op2Type,
    0.U,
    Seq(
      OPn_RF  -> io.regData.rt,
      OPn_IMM -> io.imm,
    ),
  )
  io.aluRes := MuxLookup(
    io.instType,
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
      ALU_CLO  -> (getclo(op1)),
      ALU_CLZ  -> (getclz(op1)),
    ),
  )
}
