package cpu.common

import chisel3._
import chisel3.util._
import math._
import cpu.common.Config._

object Const {
  val NONE = 0.U

  // hilo select
  val HILO_HI = true.B
  val HILO_LO = false.B

  // mul / div enable signal
  val MULDIV_ENABLE  = true.B
  val MULDIV_DISABLE = false.B

  // wb signal
  val WB_ENABLE = true.B
  val WB_DISABLE = false.B

  // ---------- Decode -------------
  // overall length config
  val OP_LEN  = 1
  val INS_LEN = 5
  val SRC_LEN = 3
  val ALU_LEN = 4
  val WRA_LEN = 2
  val IMM_LEN = 2

  // operand type
  val op_x   = 0.U(OP_LEN.W)
  val op_reg = 0.U(OP_LEN.W)
  val op_imm = 1.U(OP_LEN.W)

  // inst type
  val inst_x    = 0.U(INS_LEN.W) // nop
  val inst_aluu = 1.U(INS_LEN.W) // alu unsigned (no exception)
  val inst_alus = 2.U(INS_LEN.W) // alu signed (will throw exception)
  val inst_br   = 3.U(INS_LEN.W) // branch (with using alu output)
  val inst_lw   = 4.U(INS_LEN.W) // load word

  // is write back?
  val wb_n = false.B
  val wb_y = true.B

  // wr data source
  val src_x   = 0.U(SRC_LEN.W)
  val src_alu = 1.U(SRC_LEN.W)
  val src_rhi = 2.U(SRC_LEN.W)
  val src_rlo = 3.U(SRC_LEN.W)
  val src_mov = 4.U(SRC_LEN.W)
  val src_mem = 5.U(SRC_LEN.W)

  // alu operator
  val alu_x    = 0.U(ALU_LEN.W)
  val alu_or   = 1.U(ALU_LEN.W)
  val alu_and  = 2.U(ALU_LEN.W)
  val alu_xor  = 3.U(ALU_LEN.W)
  val alu_nor  = 4.U(ALU_LEN.W)
  val alu_sll  = 5.U(ALU_LEN.W)
  val alu_srl  = 6.U(ALU_LEN.W)
  val alu_sra  = 7.U(ALU_LEN.W)
  val alu_add  = 8.U(ALU_LEN.W)
  val alu_sub  = 9.U(ALU_LEN.W)
  val alu_slt  = 10.U(ALU_LEN.W)
  val alu_sltu = 11.U(ALU_LEN.W)
  val alu_clo  = 12.U(ALU_LEN.W)
  val alu_clz  = 13.U(ALU_LEN.W)
  val alu_mul  = 14.U(ALU_LEN.W) // -> muldiv module
  val alu_div  = 15.U(ALU_LEN.W) // -> muldiv module

  // decode only
  // how to fetch write reg address
  val wra_x   = 0.U(WRA_LEN.W) // none
  val wra_i15 = 1.U(WRA_LEN.W) // rd = inst(15, 11)
  val wra_i20 = 2.U(WRA_LEN.W) // rd = inst(20, 16)
  val wra_r31 = 3.U(WRA_LEN.W) // use $Reg(31)

  // decode only
  // how to fetch imm num
  val imm_x  = 0.U(IMM_LEN.W) // default: use shift status
  val imm_sh = 0.U(IMM_LEN.W) // use inst(10,6), no extend, for SLL, SRL, SRA
  val imm_se = 1.U(IMM_LEN.W) // use inst(15,0) for lowbit, signed extend
  val imm_ze = 2.U(IMM_LEN.W) // use inst(15,0) for lowbit, zero extend
}
