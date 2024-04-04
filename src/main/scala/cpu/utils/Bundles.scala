package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const

class BundleReg extends Bundle {
  val rs = UInt(REG_WIDTH.W)
  val rt = UInt(REG_WIDTH.W)
  val rd = UInt(REG_WIDTH.W)
}

class BundleRegData extends Bundle {
  val rs = UInt(DATA_WIDTH.W)
  val rt = UInt(DATA_WIDTH.W)
}

class BundleInst extends Bundle {
  // instruction type
  val instValid  = Bool()                         // is instruction valid
  val num1Type   = Bool()                         // operand is reg / imme / mem / none
  val num2Type   = Bool()                         // same
  val instType   = UInt(DECODE_INST_TYPE_WIDTH.W) // the instruction goes to which target
  val opType     = UInt(OP_WIDTH.W)               // the operation type
  val writeReg   = Bool()                         // is to write data into register
  val wrAddrType = UInt(DECODE_WRA_WIDTH.W)       // the write address type
  val immType    = UInt(DECODE_IMM_WIDTH.W)       // immediate number type

  // operand info
  val rs    = UInt(REG_WIDTH.W)   // reg source 1
  val rt    = UInt(REG_WIDTH.W)   // reg source 2
  val rd    = UInt(REG_WIDTH.W)   // reg destionation
  val shamt = UInt(SHAMT_WIDTH.W) // shift amount
  val imm   = UInt(DATA_WIDTH.W)   // immediate num extended
}
