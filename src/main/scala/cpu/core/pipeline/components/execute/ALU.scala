package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

class ALU extends Module {
  val io = IO(new Bundle {
    val inst = Input(new InstInfoExt)
    val op1  = Input(UInt(DATA_WIDTH.W))
    val op2  = Input(UInt(DATA_WIDTH.W))
    val ex   = Output(Bool())
    val out  = Output(UInt(DATA_WIDTH.W))
  })
  val en = io.inst.fu === fu_alu
  val op1 = MuxLookup(
    io.inst.op1,
    0.U,
    Seq(
      op_reg -> io.op1,
      op_imm -> io.inst.imm,
    ),
  )
  val op2 = MuxLookup(
    io.inst.op2,
    0.U,
    Seq(
      op_reg -> io.op2,
      op_imm -> io.inst.imm,
    ),
  )

  val WIDTH  = DATA_WIDTH + 1
  val issub  = io.inst.fuop === _alu_sub
  val op2sub = -op2

  val signop1 = signedExtend(op1, WIDTH)
  val signop2 = signedExtend(Mux(issub, op2sub, op2), WIDTH)
  val calcRes = signop1 + signop2
  val addRes  = calcRes(DATA_WIDTH - 1, 0)
  val overflow = io.inst.fuop === _alu_ex &&
    (calcRes(WIDTH - 1) ^ calcRes(WIDTH - 2))

  val orRes   = op1 | op2
  val andRes  = op1 & op2
  val xorRes  = op1 ^ op2
  val lshift  = op2 << op1(4, 0)
  val rshift  = op2 >> op1(4, 0)
  val rashift = (op2.asSInt() >> op1(4, 0)).asUInt
  val sltRes  = zeroExtend((calcRes.asSInt < 0.S).asBool)
  val sltuRes = zeroExtend(op1 < op2)

  io.out := Mux(
    en,
    MuxLookup(
      io.inst.fuop,
      0.U,
      Seq(
        alu_or   -> orRes,
        alu_and  -> andRes,
        alu_xor  -> xorRes,
        alu_nor  -> ~orRes,
        alu_sll  -> lshift,
        alu_srl  -> rshift,
        alu_sra  -> rashift,
        alu_add  -> addRes,
        alu_addu -> addRes,
        alu_sub  -> addRes,
        alu_subu -> addRes,
        alu_slt  -> sltRes,
        alu_sltu -> sltuRes,
      ),
    ),
    0.U,
  )
  io.ex := en && overflow
}
