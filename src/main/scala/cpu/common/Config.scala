package cpu.common

import chisel3._
import chisel3.util._
import math._

object Config {
  val MEM_INST_SIZE = 1024
  val MEM_DATA_SIZE = 1024

  val ADDR_WIDTH      = 32
  val ADDR_INST_WIDTH = 32
  val ADDR_BYTE_WIDTH = 4

  val DATA_WIDTH = 32
  val HILO_WIDTH = 64

  val INST_TYPE_COUNT   = 77
  val DECODE_INST_WIDTH = log2Ceil(INST_TYPE_COUNT).toInt
  val IMM_WIDTH         = 16

  val INST_WIDTH          = 32
  val INST_BYTE_WIDTH     = 4
  val INST_HOME           = "src/test/scala/instruction/memInst.hex"
  val INST_BYTE_WIDTH_LOG = log2Ceil(INST_BYTE_WIDTH).toInt

  val PC_START_ADDR: Long = 0x00000084
  val REG_NUM             = 32
  val REG_WIDTH           = log2Ceil(REG_NUM).toInt // register addr width
  val REG_ZERO_HOME       = 0
  val REG_RA31_HOME       = 31
}
