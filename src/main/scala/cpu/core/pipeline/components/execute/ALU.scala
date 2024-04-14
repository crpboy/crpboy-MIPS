package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class ALU extends Module {
  val io = IO(new Bundle {
    val inst = Input(new InstInfoExt)
    val data = Input(new RegData)
    val out  = Output(UInt(DATA_WIDTH.W))
  })
  val en = io.inst.fu === fu_alu
  val op1 = MuxLookup(
    io.inst.op1,
    0.U,
    Seq(
      op_reg -> io.data.rs,
      op_imm -> io.inst.imm,
    ),
  )
  val op2 = MuxLookup(
    io.inst.op2,
    0.U,
    Seq(
      op_reg -> io.data.rt,
      op_imm -> io.inst.imm,
    ),
  )
  io.out := Mux(
    en,
    MuxLookup(
      io.inst.fuop,
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
      ),
    ),
    0.U,
  )
}
