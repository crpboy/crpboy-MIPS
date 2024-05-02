package cpu.common

import chisel3._
import chisel3.util._
import math._

trait ConstDecode {
  // ---------- Decode -------------
  // overall length config
  val OPR_LEN       = 1
  val FU_LEN        = 4
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
  val instBA = 6.U(INST_TYPE_LEN.W) // Branch XAL
  val instSP = 7.U(INST_TYPE_LEN.W) // special inst

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
  val fu_md  = 4.U(FU_LEN.W)
  val fu_mov = 5.U(FU_LEN.W)
  val fu_mem = 6.U(FU_LEN.W)
  val fu_cp0 = 7.U(FU_LEN.W)

  // function operator
  val fuop_nop = "b0000".U

  // mul / div operator
  // (3) = 1 -> signed; (1) = 1 -> div; (0) = 1 -> mul
  val md_mult  = "b1001".U
  val md_multu = "b0001".U
  val md_div   = "b1010".U
  val md_divu  = "b0010".U

  val _md_signed = BitPat("b1???")
  val _md_div    = BitPat("b??1?")
  val _md_mul    = BitPat("b???1")

  // alu operator
  // (3)&(2)&(1) = 1 -> have exception
  val alu_add  = "b1110".U
  val alu_addu = "b0010".U
  val alu_sub  = "b1111".U
  val alu_subu = "b0011".U
  val alu_slt  = "b0001".U
  val alu_sltu = "b0101".U
  val alu_and  = "b0100".U
  val alu_nor  = "b0110".U
  val alu_or   = "b0111".U
  val alu_xor  = "b1000".U
  val alu_sll  = "b1001".U
  val alu_srl  = "b1010".U
  val alu_sra  = "b1011".U

  val _alu_ex  = BitPat("b111?")
  val _alu_sub = BitPat("b???1")

  // bra operator
  // op(3)=1 is ..al inst
  val bra_beq    = "b0001".U
  val bra_bne    = "b0010".U
  val bra_bgez   = "b0011".U
  val bra_bgtz   = "b0100".U
  val bra_blez   = "b0101".U
  val bra_bltz   = "b0110".U
  val bra_bltzal = "b1110".U
  val bra_bgezal = "b1011".U

  // jump operator
  // (2) = 1 -> read reg
  // (3) = 1 -> write reg
  val jmp_j    = "b0001".U
  val jmp_jal  = "b1001".U
  val jmp_jr   = "b0101".U
  val jmp_jalr = "b1101".U

  val _jmp_rreg = BitPat("b?1??")
  val _jmp_wreg = BitPat("b1???")

  // move operator
  // (1) = 1 -> hi; (0) = 1 -> move to
  val mov_mfhi = "b1010".U
  val mov_mflo = "b1000".U
  val mov_mthi = "b1011".U
  val mov_mtlo = "b1001".U

  val _mov_usehi = BitPat("b??1?")
  val _mov_ismt  = BitPat("b???1")

  // load & store
  // (3) = 1 -> store
  // Load.(2) = 0 -> lw
  val mem_lb  = "b0100".U
  val mem_lbu = "b0101".U
  val mem_lh  = "b0110".U
  val mem_lhu = "b0111".U
  val mem_lw  = "b0011".U
  val mem_lwl = "b0001".U
  val mem_lwr = "b0010".U
  val mem_sb  = "b1000".U
  val mem_sh  = "b1001".U
  val mem_sw  = "b1010".U
  val mem_swl = "b1011".U
  val mem_swr = "b1100".U

  val _mem_store = BitPat("b1???")
  val _mem_lw    = BitPat("b?0??")

  // cp0
  val cp0_break   = "b0001".U
  val cp0_syscall = "b0010".U
  val cp0_eret    = "b0011".U
  val cp0_mfc0    = "b0100".U
  val cp0_mtc0    = "b0101".U
}
