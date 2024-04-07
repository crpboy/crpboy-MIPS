package cpu.common

import chisel3._
import chisel3.util._
import math._
import cpu.common.Config._

object Const {
  // 大写的是Config里的设置
  // 小写的是Const里的译码阶段需要的信号

  // overall length config
  val op_len  = 2
  val src_len = 3
  val alu_len = 5
  val wra_len = 2
  val imm_len = 2
  val sp_len  = 2

  // operand type
  val op_x   = 0.U(op_len.W)
  val op_reg = 1.U(op_len.W)
  val op_imm = 2.U(op_len.W)

  // wr data source from
  val src_x   = 0.U(src_len.W)
  val src_alu = 1.U(src_len.W)
  val src_rhi = 2.U(src_len.W)
  val src_rlo = 3.U(src_len.W)
  val src_mov = 4.U(src_len.W)
  val src_mem = 5.U(src_len.W)

  // alu operator
  val alu_x    = 0.U(alu_len.W)
  val alu_or   = 1.U(alu_len.W)
  val alu_and  = 2.U(alu_len.W)
  val alu_xor  = 3.U(alu_len.W)
  val alu_nor  = 4.U(alu_len.W)
  val alu_sll  = 5.U(alu_len.W)
  val alu_srl  = 6.U(alu_len.W)
  val alu_sra  = 7.U(alu_len.W)
  val alu_add  = 8.U(alu_len.W)
  val alu_addu = 9.U(alu_len.W)
  val alu_sub  = 10.U(alu_len.W)
  val alu_subu = 11.U(alu_len.W)
  val alu_mul  = 12.U(alu_len.W)
  val alu_div  = 13.U(alu_len.W)
  val alu_slt  = 14.U(alu_len.W)
  val alu_sltu = 15.U(alu_len.W)
  val alu_clo  = 16.U(alu_len.W)
  val alu_clz  = 17.U(alu_len.W)

  // how to fetch write reg address
  val wra_x   = 0.U(wra_len.W) // none
  val wra_i15 = 1.U(wra_len.W) // rd = inst(15, 11)
  val wra_i20 = 2.U(wra_len.W) // rd = inst(20, 16)
  val wra_r31 = 3.U(wra_len.W) // use $Reg(31)

  // how to fetch imm num
  val imm_x  = 0.U(imm_len.W) // default: use shift status
  val imm_sh = 0.U(imm_len.W) // use inst(10,6), no extend, for SLL, SRL, SRA
  val imm_se = 1.U(imm_len.W) // use inst(15,0) for lowbit, signed extend
  val imm_ze = 2.U(imm_len.W) // use inst(15,0) for lowbit, zero extend

  // read memory
  val mem_n = false.B
  val mem_y = true.B

  // is jump / branch
  val jmp_n = false.B
  val jmp_y = true.B

  // special instruction
  val sp_x   = 0.U(sp_len.W)
  val sp_lui = 1.U(sp_len.W) // LUI
}

// object deletedConst {
//   private val srcLen  = DECODE_SRC_WIDTH
//   private val instLen = DECODE_INST_WIDTH
//   private val wraLen  = DECODE_WRA_WIDTH
//   private val immLen  = DECODE_IMM_WIDTH

//   // is instruction valid
//   val Y = true.B
//   val N = false.B

//   // Operand
//   val OPn_RF  = 0.U(1.W) // regfile
//   val OPn_IMM = 1.U(1.W) // immediate
//   val OPn_X   = 1.U(1.W) // dont care

//   // Instruction src type
//   // 指令类型（判断执行阶段写入寄存器结果的来源 ALU, CP0...）
//   val INST_N    = 0.U(srcLen.W)
//   val INST_ALU  = 1.U(srcLen.W) // 普通ALU,
//   val INST_MV   = 2.U(srcLen.W) // Move（包括hilo, cp0)
//   val INST_WO   = 3.U(srcLen.W) // 写入其它寄存器（hilo, cp0)
//   val INST_MEM  = 4.U(srcLen.W) // 访存
//   val INST_BR   = 5.U(srcLen.W) // 跳转指令
//   val INST_EXC  = 6.U(srcLen.W) // 例外指令
//   val INST_TLB  = 7.U(srcLen.W) // TLB instruction
//   val INST_TRAP = 8.U(srcLen.W)

//   val OP_N = 0.U(instLen.W) // 无操作

//   // ALU type
//   val ALU_OR   = 1.U(instLen.W)  // OR
//   val ALU_AND  = 2.U(instLen.W)  // AND
//   val ALU_XOR  = 3.U(instLen.W)  // XOR
//   val ALU_NOR  = 4.U(instLen.W)  // NOR
//   val ALU_SLL  = 5.U(instLen.W)  // Shift Left Logical
//   val ALU_SRL  = 6.U(instLen.W)  // Shift Light Logical
//   val ALU_SRA  = 7.U(instLen.W)  // Shift Right Arithmetic
//   val ALU_SLT  = 8.U(instLen.W)  // Set on Less Than
//   val ALU_SLTU = 9.U(instLen.W)  // Set on Less Than unsigned
//   val ALU_ADD  = 10.U(instLen.W) // ADD
//   val ALU_ADDU = 11.U(instLen.W) // ADD Unsigned
//   val ALU_SUB  = 12.U(instLen.W) // SUB
//   val ALU_SUBU = 13.U(instLen.W) // SUB Unsigned

//   // Move
//   val MV_MFHI = 14.U(instLen.W)
//   val MV_MFLO = 15.U(instLen.W)
//   val MV_MFC0 = 16.U(instLen.W)

//   // write to other
//   val WO_MTC0  = 17.U(instLen.W) // only used in decode
//   val WO_MTLO  = 18.U(instLen.W)
//   val WO_MTHI  = 19.U(instLen.W)
//   val WO_MULT  = 20.U(instLen.W)
//   val WO_MULTU = 21.U(instLen.W)
//   val WO_DIV   = 22.U(instLen.W)
//   val WO_DIVU  = 23.U(instLen.W)

//   // Memory
//   val MEM_LB  = 24.U(instLen.W)
//   val MEM_LBU = 25.U(instLen.W)
//   val MEM_LH  = 26.U(instLen.W)
//   val MEM_LHU = 27.U(instLen.W)
//   val MEM_LW  = 28.U(instLen.W)
//   val MEM_SB  = 29.U(instLen.W)
//   val MEM_SH  = 30.U(instLen.W)
//   val MEM_SW  = 31.U(instLen.W)

//   // Branch
//   val BR_JR    = 32.U(instLen.W) // Jump Register
//   val BR_JALR  = 33.U(instLen.W) // Jump Register and Link
//   val BR_J     = 34.U(instLen.W) // Jump
//   val BR_JAL   = 35.U(instLen.W) // Jump and Link
//   val BR_EQ    = 36.U(instLen.W) // Branch on Equal
//   val BR_NE    = 37.U(instLen.W) // Branch on Not Equal
//   val BR_GTZ   = 38.U(instLen.W) // Branch on Greater Than Zero
//   val BR_GEZ   = 39.U(instLen.W) // Branch on Greater/Equal Than Zero
//   val BR_GEZAL = 40.U(instLen.W) // Branch on Greater/Equal Than Zero and Link
//   val BR_LTZ   = 41.U(instLen.W) // Branch on Less Than Zero
//   val BR_LTZAL = 42.U(instLen.W) // Branch on Less Than Zero and Link
//   val BR_LEZ   = 43.U(instLen.W) // Branch on Less/Equal Than Zero

//   // Except
//   val EXC_SC = 44.U(instLen.W) // syscall 系统调用
//   val EXC_ER = 45.U(instLen.W) // eret 返回
//   val EXC_BR = 46.U(instLen.W) // break 中断

//   // TLB type
//   val TLB_WI   = 47.U(instLen.W)
//   val TLB_WR   = 48.U(instLen.W)
//   val TLB_P    = 49.U(instLen.W)
//   val TLB_R    = 50.U(instLen.W)
//   val ALU_MUL  = 51.U(instLen.W)
//   val MV_MOVN  = 52.U(instLen.W)
//   val MV_MOVZ  = 53.U(instLen.W)
//   val MEM_CAC  = 54.U(instLen.W)
//   val ALU_CLO  = 55.U(instLen.W)
//   val ALU_CLZ  = 56.U(instLen.W)
//   val MEM_LWL  = 57.U(instLen.W)
//   val MEM_LWR  = 58.U(instLen.W)
//   val MEM_SWL  = 59.U(instLen.W)
//   val MEM_SWR  = 60.U(instLen.W)
//   val EXC_WAIT = 61.U(instLen.W)
//   val WO_MADD  = 62.U(instLen.W)
//   val WO_MADDU = 63.U(instLen.W)
//   val WO_MSUB  = 64.U(instLen.W)
//   val WO_MSUBU = 65.U(instLen.W)
//   val MEM_LL   = 66.U(instLen.W)
//   val MEM_SC   = 67.U(instLen.W)

//   // trap
//   val TRAP_EQ  = 1.U(instLen.W)
//   val TRAP_GE  = 2.U(instLen.W)
//   val TRAP_GEU = 3.U(instLen.W)
//   val TRAP_LT  = 4.U(instLen.W)
//   val TRAP_LTU = 5.U(instLen.W)
//   val TRAP_NE  = 6.U(instLen.W)

//   // Write Register
//   val WR_Y = true.B
//   val WR_N = false.B

//   // Write Register Address type
//   val WRA_T1 = 0.U(wraLen.W) // inst(15,11)
//   val WRA_T2 = 1.U(wraLen.W) // inst(20,16)
//   val WRA_T3 = 2.U(wraLen.W) // "b11111", 即31号寄存器
//   val WRA_X  = 0.U(wraLen.W) // not care

//   // imm type
//   val IMM_N   = 0.U(immLen.W)
//   val IMM_LSE = 1.U(immLen.W) // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
//   val IMM_LZE = 2.U(immLen.W) // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
//   val IMM_HZE = 3.U(immLen.W) // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
//   val IMM_SHT = 4.U(immLen.W) // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
// }
