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

class BundleDecoupled extends Bundle {
  val valid = Input(Bool())
  val ready = Output(Bool())
}

// class BundleInst extends Bundle {
//   // instruction type
//   val instValid = Bool()                    // is instruction valid
//   val op1Type   = Bool()                    // operand is reg / imme / mem / none
//   val op2Type   = Bool()                    // same
//   val srcType   = UInt(DECODE_SRC_WIDTH.W)  // reg read src
//   val instType  = UInt(DECODE_INST_WIDTH.W) // the operation type
//   val writeReg  = Bool()                    // is to write data into register
//   // [DELETED] val wrAddrType = UInt(DECODE_WRA_WIDTH.W) // the write address type
//   // [DELETED] val immType    = UInt(DECODE_IMM_WIDTH.W) // immediate number type

//   // operand info
//   val regAddr = new BundleReg
//   val shamt   = UInt(SHAMT_WIDTH.W) // shift amount
//   val imm     = UInt(DATA_WIDTH.W)  // immediate num extended
// }
