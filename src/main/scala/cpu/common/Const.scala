package cpu.common

import chisel3._
import chisel3.util._
import math._
import cpu.common.Config._

object Const {
  private val instTypeLen = DECODE_INST_TYPE_WIDTH
  private val opLen       = OP_WIDTH
  private val wraLen      = DECODE_WRA_WIDTH
  private val immLen      = DECODE_IMM_WIDTH

  // is instruction valid
  val Y = true.B
  val N = false.B

  // Operand
  val OPn_RF  = 0.U(1.W) // regfile
  val OPn_IMM = 1.U(1.W) // immediate
  val OPn_X   = 1.U(1.W) // dont care

  // Instruction type
  // 指令类型（判断执行阶段写入寄存器结果的来源 ALU, CP0...）
  val INST_N    = 0.U(instTypeLen.W)
  val INST_ALU  = 1.U(instTypeLen.W) // 普通ALU,
  val INST_MV   = 2.U(instTypeLen.W) // Move（包括hilo, cp0)
  val INST_WO   = 3.U(instTypeLen.W) // 写入其它寄存器（hilo, cp0)
  val INST_MEM  = 4.U(instTypeLen.W) // 访存
  val INST_BR   = 5.U(instTypeLen.W) // 跳转指令
  val INST_EXC  = 6.U(instTypeLen.W) // 例外指令
  val INST_TLB  = 7.U(instTypeLen.W) // TLB instruction
  val INST_TRAP = 8.U(instTypeLen.W)

  val OP_N = 0.U(opLen.W) // 无操作

  // ALU type
  val ALU_OR   = 1.U(opLen.W)  // OR
  val ALU_AND  = 2.U(opLen.W)  // AND
  val ALU_XOR  = 3.U(opLen.W)  // XOR
  val ALU_NOR  = 4.U(opLen.W)  // NOR
  val ALU_SLL  = 5.U(opLen.W)  // Shift Left Logical
  val ALU_SRL  = 6.U(opLen.W)  // Shift Light Logical
  val ALU_SRA  = 7.U(opLen.W)  // Shift Right Arithmetic
  val ALU_SLT  = 8.U(opLen.W)  // Set on Less Than
  val ALU_SLTU = 9.U(opLen.W)  // Set on Less Than unsigned
  val ALU_ADD  = 10.U(opLen.W) // ADD
  val ALU_ADDU = 11.U(opLen.W) // ADD Unsigned
  val ALU_SUB  = 12.U(opLen.W) // SUB
  val ALU_SUBU = 13.U(opLen.W) // SUB Unsigned

  // Move
  val MV_MFHI = 14.U(opLen.W)
  val MV_MFLO = 15.U(opLen.W)
  val MV_MFC0 = 16.U(opLen.W)

  // write to other
  val WO_MTC0  = 17.U(opLen.W) // only used in decode
  val WO_MTLO  = 18.U(opLen.W)
  val WO_MTHI  = 19.U(opLen.W)
  val WO_MULT  = 20.U(opLen.W)
  val WO_MULTU = 21.U(opLen.W)
  val WO_DIV   = 22.U(opLen.W)
  val WO_DIVU  = 23.U(opLen.W)

  // Memory
  val MEM_LB  = 24.U(opLen.W)
  val MEM_LBU = 25.U(opLen.W)
  val MEM_LH  = 26.U(opLen.W)
  val MEM_LHU = 27.U(opLen.W)
  val MEM_LW  = 28.U(opLen.W)
  val MEM_SB  = 29.U(opLen.W)
  val MEM_SH  = 30.U(opLen.W)
  val MEM_SW  = 31.U(opLen.W)

  // Branch
  val BR_JR    = 32.U(opLen.W) // Jump Register
  val BR_JALR  = 33.U(opLen.W) // Jump Register and Link
  val BR_J     = 34.U(opLen.W) // Jump
  val BR_JAL   = 35.U(opLen.W) // Jump and Link
  val BR_EQ    = 36.U(opLen.W) // Branch on Equal
  val BR_NE    = 37.U(opLen.W) // Branch on Not Equal
  val BR_GTZ   = 38.U(opLen.W) // Branch on Greater Than Zero
  val BR_GEZ   = 39.U(opLen.W) // Branch on Greater/Equal Than Zero
  val BR_GEZAL = 40.U(opLen.W) // Branch on Greater/Equal Than Zero and Link
  val BR_LTZ   = 41.U(opLen.W) // Branch on Less Than Zero
  val BR_LTZAL = 42.U(opLen.W) // Branch on Less Than Zero and Link
  val BR_LEZ   = 43.U(opLen.W) // Branch on Less/Equal Than Zero

  // Except
  val EXC_SC = 44.U(opLen.W) // syscall 系统调用
  val EXC_ER = 45.U(opLen.W) // eret 返回
  val EXC_BR = 46.U(opLen.W) // break 中断

  // TLB type
  val TLB_WI   = 47.U(opLen.W)
  val TLB_WR   = 48.U(opLen.W)
  val TLB_P    = 49.U(opLen.W)
  val TLB_R    = 50.U(opLen.W)
  val ALU_MUL  = 51.U(opLen.W)
  val MV_MOVN  = 52.U(opLen.W)
  val MV_MOVZ  = 53.U(opLen.W)
  val MEM_CAC  = 54.U(opLen.W)
  val ALU_CLO  = 55.U(opLen.W)
  val ALU_CLZ  = 56.U(opLen.W)
  val MEM_LWL  = 57.U(opLen.W)
  val MEM_LWR  = 58.U(opLen.W)
  val MEM_SWL  = 59.U(opLen.W)
  val MEM_SWR  = 60.U(opLen.W)
  val EXC_WAIT = 61.U(opLen.W)
  val WO_MADD  = 62.U(opLen.W)
  val WO_MADDU = 63.U(opLen.W)
  val WO_MSUB  = 64.U(opLen.W)
  val WO_MSUBU = 65.U(opLen.W)
  val MEM_LL   = 66.U(opLen.W)
  val MEM_SC   = 67.U(opLen.W)

  // trap
  val TRAP_EQ  = 1.U(opLen.W)
  val TRAP_GE  = 2.U(opLen.W)
  val TRAP_GEU = 3.U(opLen.W)
  val TRAP_LT  = 4.U(opLen.W)
  val TRAP_LTU = 5.U(opLen.W)
  val TRAP_NE  = 6.U(opLen.W)

  // Write Register
  val WR_Y = true.B
  val WR_N = false.B

  // Write Register Address type
  val WRA_T1 = 0.U(wraLen.W) // inst(15,11)
  val WRA_T2 = 1.U(wraLen.W) // inst(20,16)
  val WRA_T3 = 2.U(wraLen.W) // "b11111", 即31号寄存器
  val WRA_X  = 0.U(wraLen.W) // not care

  // imm type
  val IMM_N   = 0.U(immLen.W)
  val IMM_LSE = 1.U(immLen.W) // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE = 2.U(immLen.W) // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE = 3.U(immLen.W) // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT = 4.U(immLen.W) // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
}
