package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

class ALU extends Module {
  val io = IO(new Bundle {
    val inst = Input(new InstInfoExt)
    val rs   = Input(UInt(DATA_WIDTH.W))
    val rt   = Input(UInt(DATA_WIDTH.W))
    val ex   = Output(Bool())
    val out  = Output(UInt(DATA_WIDTH.W))
  })
  val en = io.inst.fu === fu_alu
  val op1 = MuxLookup(
    io.inst.op1,
    0.U,
    Seq(
      op_reg -> io.rs,
      op_imm -> io.inst.imm,
    ),
  )
  val op2 = MuxLookup(
    io.inst.op2,
    0.U,
    Seq(
      op_reg -> io.rt,
      op_imm -> io.inst.imm,
    ),
  )

  val WIDTH     = DATA_WIDTH + 2
  val extop1    = signedExtend(op1, WIDTH)
  val extop2    = signedExtend(op2, WIDTH)
  val extop2Sub = signedExtend(-op2, WIDTH)
  val calcRes   = extop1 + Mux(io.inst.fuop === _alu_sub, extop2Sub, extop2)
  val addRes    = calcRes(DATA_WIDTH - 1, 0)
  val overflow  = calcRes(WIDTH - 1) ^ calcRes(WIDTH - 2)

  val orRes   = op1 | op2
  val andRes  = op1 & op2
  val xorRes  = op1 ^ op2
  val lshift  = op2 << op1(4, 0)
  val rshift  = op2 >> op1(4, 0)
  val rashift = (op2.asSInt() >> op1(4, 0)).asUInt
  val sltRes  = zeroExtend((calcRes.asSInt < 0.S).asBool)
  val sltuRes = zeroExtend(op1 < op2) // TODO: 改成使用减法结果

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
  io.ex := en && io.inst.fuop === _alu_ex && overflow
}
