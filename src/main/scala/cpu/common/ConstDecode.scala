package cpu.common

import chisel3._
import chisel3.util._
import math._

trait ConstDecode {
  // ---------- Decode -------------
  // overall length config
  val OPR_LEN       = 1
  val FU_LEN        = 3
  val FUOP_LEN      = 4
  val WRA_LEN       = 2
  val IMM_LEN       = 3
  val INST_TYPE_LEN = 3

  // valid / invalid
  val iy          = true.B
  val instInvalid = false.B

  // decode only
  // inst type
  val instRN = 0.U(INST_TYPE_LEN.W) // reg without imm
  val instRS = 1.U(INST_TYPE_LEN.W) // reg with shamt
  val instIS = 2.U(INST_TYPE_LEN.W) // imm signed extend
  val instIZ = 3.U(INST_TYPE_LEN.W) // imm zero extend
  val instIL = 4.U(INST_TYPE_LEN.W) // imm lui
  val instJP = 5.U(INST_TYPE_LEN.W) // jump
  val instSP = 6.U(INST_TYPE_LEN.W) // special inst

  // operand type
  val op_x   = 0.U(OPR_LEN.W)
  val op_reg = 0.U(OPR_LEN.W)
  val op_imm = 1.U(OPR_LEN.W)

  // is write back?
  val wb_n = false.B
  val wb_y = true.B

  // function
  val fu_nop = 0.U(FU_LEN.W)
  val fu_alu = 1.U(FU_LEN.W)
  val fu_bra = 2.U(FU_LEN.W)
  val fu_jmp = 3.U(FU_LEN.W)
  val fu_mul = 4.U(FU_LEN.W)
  val fu_mov = 5.U(FU_LEN.W)
  val fu_mem = 6.U(FU_LEN.W)
  val fu_oth = 7.U(FU_LEN.W)

  // function operator
  val fuop_n = 0.U(FUOP_LEN.W)
  // mul / div operator
  val md_mult  = 1.U(FUOP_LEN.W)
  val md_multu = 2.U(FUOP_LEN.W)
  val md_div   = 3.U(FUOP_LEN.W)
  val md_divu  = 4.U(FUOP_LEN.W)
  // alu operator
  val alu_add  = 1.U(FUOP_LEN.W)
  val alu_addu = 2.U(FUOP_LEN.W)
  val alu_sub  = 3.U(FUOP_LEN.W)
  val alu_subu = 4.U(FUOP_LEN.W)
  val alu_slt  = 5.U(FUOP_LEN.W)
  val alu_sltu = 6.U(FUOP_LEN.W)
  val alu_and  = 7.U(FUOP_LEN.W)
  val alu_nor  = 8.U(FUOP_LEN.W)
  val alu_or   = 9.U(FUOP_LEN.W)
  val alu_xor  = 10.U(FUOP_LEN.W)
  val alu_sll  = 11.U(FUOP_LEN.W)
  val alu_srl  = 12.U(FUOP_LEN.W)
  val alu_sra  = 13.U(FUOP_LEN.W)
  // bra operator
  val bra_beq    = 1.U(FUOP_LEN.W)
  val bra_bne    = 2.U(FUOP_LEN.W)
  val bra_bgez   = 3.U(FUOP_LEN.W)
  val bra_bgtz   = 4.U(FUOP_LEN.W)
  val bra_blez   = 5.U(FUOP_LEN.W)
  val bra_bltz   = 6.U(FUOP_LEN.W)
  val bra_bltzal = 7.U(FUOP_LEN.W)
  val bra_bgezal = 8.U(FUOP_LEN.W)
  // jump operator
  val jmp_j    = 1.U(FUOP_LEN.W)
  val jmp_jal  = 2.U(FUOP_LEN.W)
  val jmp_jr   = 3.U(FUOP_LEN.W)
  val jmp_jalr = 4.U(FUOP_LEN.W)
  // move operator
  val mov_mfhi = 1.U(FUOP_LEN.W)
  val mov_mflo = 2.U(FUOP_LEN.W)
  val mov_mthi = 3.U(FUOP_LEN.W)
  val mov_mtlo = 4.U(FUOP_LEN.W)
  // load & store
  val mem_lb  = 1.U(FUOP_LEN.W)
  val mem_lbu = 2.U(FUOP_LEN.W)
  val mem_lh  = 3.U(FUOP_LEN.W)
  val mem_lhu = 4.U(FUOP_LEN.W)
  val mem_lw  = 5.U(FUOP_LEN.W)
  val mem_lwl = 6.U(FUOP_LEN.W)
  val mem_lwr = 7.U(FUOP_LEN.W)
  val mem_sb  = 8.U(FUOP_LEN.W)
  val mem_sh  = 9.U(FUOP_LEN.W)
  val mem_sw  = 10.U(FUOP_LEN.W)
  val mem_swl = 11.U(FUOP_LEN.W)
  val mem_swr = 12.U(FUOP_LEN.W)
  // other functions
  val oth_lui     = 1.U(FUOP_LEN.W)
  val oth_break   = 2.U(FUOP_LEN.W)
  val oth_syscall = 3.U(FUOP_LEN.W)

  // [[discard]]
  // decode only
  // how to fetch write reg address
  // val wra_x   = 0.U(WRA_LEN.W) // none
  // val wra_i15 = 1.U(WRA_LEN.W) // rd = inst(15, 11)
  // val wra_i20 = 2.U(WRA_LEN.W) // rd = inst(20, 16)
  // val wra_r31 = 3.U(WRA_LEN.W) // use $Reg(31)

  // [[discard]]
  // decode only
  // how to fetch imm num
  // val imm_x   = 0.U(IMM_LEN.W) // default: use shift status
  // val imm_sh  = 0.U(IMM_LEN.W) // use inst(10,6), no extend, for SLL, SRL, SRA
  // val imm_se  = 1.U(IMM_LEN.W) // use inst(15,0), signed extend
  // val imm_ze  = 2.U(IMM_LEN.W) // use inst(15,0), zero extend
  // val imm_j   = 3.U(IMM_LEN.W) // use inst(25,0), zero extend << 2
  // val imm_lui = 4.U(IMM_LEN.W) // lui, high zero extend
}
