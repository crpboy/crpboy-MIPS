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
  val fu_oth = 7.U(FU_LEN.W)

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
  val alu_addu = "b0001".U
  val alu_sub  = "b1111".U
  val alu_subu = "b0010".U
  val alu_slt  = "b0011".U
  val alu_sltu = "b0100".U
  val alu_and  = "b0101".U
  val alu_nor  = "b0110".U
  val alu_or   = "b0111".U
  val alu_xor  = "b1000".U
  val alu_sll  = "b1001".U
  val alu_srl  = "b1010".U
  val alu_sra  = "b1011".U

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

  // other functions
  val oth_lui     = "b1000".U
  val oth_break   = "b0001".U
  val oth_syscall = "b0010".U

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
