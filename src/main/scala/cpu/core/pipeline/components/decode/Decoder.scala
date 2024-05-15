package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._

import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class Decoder extends Module {
  val io = IO(new Bundle {
    val rawInst  = Input(UInt(INST_WIDTH.W))
    val instInfo = Output(new InstInfoExt)
    val rsaddr   = Output(UInt(REG_WIDTH.W))
    val rtaddr   = Output(UInt(REG_WIDTH.W))
    val isex     = Output(Bool())
  })
  val inst = io.rawInst
  val res: List[UInt] = ListLookup(
    io.rawInst,
    List(instInvalid, instRN, wb_n, fu_nop, fuop_nop),
    Array(
      NOP -> List(iy, instRN, wb_n, fu_nop, fuop_nop),
      // arithmetic
      ADD   -> List(iy, instRN, wb_y, fu_alu, alu_add),
      ADDI  -> List(iy, instIS, wb_y, fu_alu, alu_add),
      ADDU  -> List(iy, instRN, wb_y, fu_alu, alu_addu),
      ADDIU -> List(iy, instIS, wb_y, fu_alu, alu_addu),
      SUB   -> List(iy, instRN, wb_y, fu_alu, alu_sub),
      SUBU  -> List(iy, instRN, wb_y, fu_alu, alu_subu),
      SLT   -> List(iy, instRN, wb_y, fu_alu, alu_slt),
      SLTI  -> List(iy, instIS, wb_y, fu_alu, alu_slt),
      SLTU  -> List(iy, instRN, wb_y, fu_alu, alu_sltu),
      SLTIU -> List(iy, instIS, wb_y, fu_alu, alu_sltu),
      DIV   -> List(iy, instRN, wb_y, fu_md, md_div),
      DIVU  -> List(iy, instRN, wb_y, fu_md, md_divu),
      MULT  -> List(iy, instRN, wb_n, fu_md, md_mult),
      MULTU -> List(iy, instRN, wb_n, fu_md, md_multu),
      // logical
      AND  -> List(iy, instRN, wb_y, fu_alu, alu_and),
      ANDI -> List(iy, instIZ, wb_y, fu_alu, alu_and),
      LUI  -> List(iy, instIL, wb_y, fu_alu, alu_or),
      NOR  -> List(iy, instRN, wb_y, fu_alu, alu_nor),
      OR   -> List(iy, instRN, wb_y, fu_alu, alu_or),
      ORI  -> List(iy, instIZ, wb_y, fu_alu, alu_or),
      XOR  -> List(iy, instRN, wb_y, fu_alu, alu_xor),
      XORI -> List(iy, instIZ, wb_y, fu_alu, alu_xor),
      // shift
      SLLV -> List(iy, instRN, wb_y, fu_alu, alu_sll),
      SLL  -> List(iy, instRS, wb_y, fu_alu, alu_sll),
      SRAV -> List(iy, instRN, wb_y, fu_alu, alu_sra),
      SRA  -> List(iy, instRS, wb_y, fu_alu, alu_sra),
      SRLV -> List(iy, instRN, wb_y, fu_alu, alu_srl),
      SRL  -> List(iy, instRS, wb_y, fu_alu, alu_srl),
      // branch
      BEQ    -> List(iy, instIS, wb_n, fu_bra, bra_beq),
      BNE    -> List(iy, instIS, wb_n, fu_bra, bra_bne),
      BGEZ   -> List(iy, instIS, wb_n, fu_bra, bra_bgez),
      BGTZ   -> List(iy, instIS, wb_n, fu_bra, bra_bgtz),
      BLEZ   -> List(iy, instIS, wb_n, fu_bra, bra_blez),
      BLTZ   -> List(iy, instIS, wb_n, fu_bra, bra_bltz),
      BLTZAL -> List(iy, instBA, wb_y, fu_bra, bra_bltzal),
      BGEZAL -> List(iy, instBA, wb_y, fu_bra, bra_bgezal),
      // jump
      J    -> List(iy, instJP, wb_n, fu_jmp, jmp_j),
      JAL  -> List(iy, instJP, wb_y, fu_jmp, jmp_jal),
      JR   -> List(iy, instRN, wb_n, fu_jmp, jmp_jr),
      JALR -> List(iy, instRN, wb_y, fu_jmp, jmp_jalr),
      // move
      MFHI -> List(iy, instRN, wb_y, fu_mov, mov_mfhi),
      MFLO -> List(iy, instRN, wb_y, fu_mov, mov_mflo),
      MTHI -> List(iy, instRN, wb_n, fu_mov, mov_mthi),
      MTLO -> List(iy, instRN, wb_n, fu_mov, mov_mtlo),
      // exception
      BREAK   -> List(iy, instSP, wb_n, fu_cp0, cp0_break),
      SYSCALL -> List(iy, instSP, wb_n, fu_cp0, cp0_syscall),
      // load
      LB  -> List(iy, instIS, wb_y, fu_mem, mem_lb),
      LBU -> List(iy, instIS, wb_y, fu_mem, mem_lbu),
      LH  -> List(iy, instIS, wb_y, fu_mem, mem_lh),
      LHU -> List(iy, instIS, wb_y, fu_mem, mem_lhu),
      LW  -> List(iy, instIS, wb_y, fu_mem, mem_lw),
      LWL -> List(iy, instIS, wb_y, fu_mem, mem_lwl),
      LWR -> List(iy, instIS, wb_y, fu_mem, mem_lwr),
      // store
      SB  -> List(iy, instIS, wb_n, fu_mem, mem_sb),
      SH  -> List(iy, instIS, wb_n, fu_mem, mem_sh),
      SW  -> List(iy, instIS, wb_n, fu_mem, mem_sw),
      SWL -> List(iy, instIS, wb_n, fu_mem, mem_swl),
      SWR -> List(iy, instIS, wb_n, fu_mem, mem_swr),
      // cp0
      ERET -> List(iy, instRN, wb_n, fu_cp0, cp0_eret),
      MFC0 -> List(iy, instRN, wb_y, fu_cp0, cp0_mfc0),
      MTC0 -> List(iy, instRN, wb_n, fu_cp0, cp0_mtc0),
    ),
  )
  val validInst :: t :: wb :: fu :: fuop :: Nil = res

  // get operand info
  val op1 = MuxLookup(t, op_reg)(
    Seq(
      instRN -> op_reg,
      instRS -> op_imm,
      instIS -> op_reg,
      instIZ -> op_reg,
      instBA -> op_reg,
      instIL -> op_reg,
    ),
  )
  val op2 = MuxLookup(t, op_reg)(
    Seq(
      instRN -> op_reg,
      instRS -> op_reg,
      instIS -> op_imm,
      instIZ -> op_imm,
      instIL -> op_imm,
    ),
  )

  // decoded info
  io.instInfo.op1  := op1
  io.instInfo.op2  := op2
  io.instInfo.wb   := wb
  io.instInfo.fu   := fu
  io.instInfo.fuop := fuop

  // reg
  io.rsaddr := inst(25, 21)
  io.rtaddr := inst(20, 16)
  io.instInfo.rd := MuxLookup(t, 0.U)(
    Seq(
      instRN -> inst(15, 11),
      instRS -> inst(15, 11),
      instIS -> inst(20, 16),
      instIZ -> inst(20, 16),
      instIL -> inst(20, 16),
      instBA -> 31.U,
      instJP -> 31.U,
    ),
  )

  // imm
  io.instInfo.imm := MuxLookup(t, 0.U)(
    Seq(
      instRN -> zeroExtend(inst(10, 0)),
      instRS -> zeroExtend(inst(10, 6)),
      instIS -> signedExtend(inst(15, 0)),
      instIZ -> zeroExtend(inst(15, 0)),
      instJP -> signedExtend(inst(25, 0)),
      instBA -> signedExtend(inst(15, 0)),
      instIL -> zeroExtendHigh(inst(15, 0)),
    ),
  )

  io.isex := !validInst
}
