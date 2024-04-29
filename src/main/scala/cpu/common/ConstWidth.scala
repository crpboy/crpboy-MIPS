package cpu.common

import chisel3._
import chisel3.util._
import math._

trait ConstWidth {
  val ADDR_WIDTH        = 32
  val ADDR_INST_WIDTH   = 32
  val ADDR_BYTE_WIDTH   = 4
  val DATA_WIDTH        = 32
  val HILO_WIDTH        = 64
  val WEN_WIDTH         = 4
  val INST_TYPE_COUNT   = 77
  val DECODE_INST_WIDTH = log2Ceil(INST_TYPE_COUNT).toInt
  val RAW_IMM_WIDTH     = 16
  val INST_WIDTH        = 32
  val PC_WIDTH          = 32
  val REG_NUM           = 32
  val REG_WIDTH         = log2Ceil(REG_NUM).toInt
  val CTRL_WIDTH        = 5
}
