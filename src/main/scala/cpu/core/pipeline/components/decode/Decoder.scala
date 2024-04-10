package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Instructions._
import cpu.common.Const._
import cpu.utils._
import cpu.utils.Functions._

class DecoderIO extends Bundle {
  val en       = Input(Bool())
  val rawInst  = Input(UInt(INST_WIDTH.W))
  val instInfo = Output(new InstInfoEXE)
}

class Decoder extends Module {
  val io   = IO(new DecoderIO)
  val inst = io.rawInst
  val res: List[UInt] = ListLookup(
    io.rawInst, // op1, op2, wb, srcWR, aluOpt, wraType, immType
    List(op_reg, op_reg, wb_n, inst_aluu, src_alu, alu_x, wra_x, imm_x),
    Array(
      OR  -> List(op_reg, op_reg, wb_y, inst_aluu, src_alu, alu_or, wra_i15, imm_x),
      ORI -> List(op_reg, op_imm, wb_n, inst_aluu, src_alu, alu_or, wra_i15, imm_sh),
    ),
  )
  val op1Type :: op2Type :: wb :: instType :: srcType :: aluType :: wraType :: immType :: Nil = res

  // decoded info
  io.instInfo.op1Type  := op1Type
  io.instInfo.op2Type  := op2Type
  io.instInfo.wb       := wb
  io.instInfo.instType := instType
  io.instInfo.srcType  := srcType
  io.instInfo.aluType  := aluType

  // reg fetch
  io.instInfo.regAddr.rs := inst(25, 21)
  io.instInfo.regAddr.rt := inst(20, 16)
  io.instInfo.rd := MuxLookup(
    wraType,
    0.U,
    Seq(
      wra_x   -> REG_ZERO_HOME.U,
      wra_i15 -> inst(15, 11),
      wra_i20 -> inst(20, 16),
      wra_r31 -> REG_RA31_HOME.U,
    ),
  )

  // imm fetch
  io.instInfo.imm := MuxLookup(
    immType,
    zeroExtend(inst(10, 6)),
    Seq(
      imm_sh -> zeroExtend(inst(10, 6)), // shift
      imm_se -> signExtend(inst(15, 0)), // signed extend
      imm_ze -> zeroExtend(inst(15, 0)), // zero extend
    ),
  )
}
