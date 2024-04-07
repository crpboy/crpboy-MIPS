package cpu.common

import chisel3._
import chisel3.util._
import math._

object Config {
  val MEM_INST_SIZE       = 1024                                     // 指令内存大小
  val MEM_DATA_SIZE       = 1024                                     // 数据内存大小
  val ADDR_WIDTH          = 32                                       // 地址位宽
  val ADDR_INST_WIDTH     = 32                                       // 指令地址位宽
  val ADDR_BYTE_WIDTH     = 4                                        // 地址位宽按字节算
  val DATA_WIDTH          = 32                                       // 数据位宽
  val INST_TYPE_COUNT     = 77                                       // DECODE 指令个数
  val DECODE_INST_WIDTH   = log2Ceil(INST_TYPE_COUNT).toInt          // DECODE 指令类型
  val IMM_WIDTH           = 16                                       // 立即数位宽
  val INST_WIDTH          = 32                                       // 指令位宽
  val INST_BYTE_WIDTH     = 4                                        // 指令位宽按字节算
  val INST_HOME           = "src/test/scala/instruction/memInst.hex" // 指令测试文件路径
  val INST_BYTE_WIDTH_LOG = log2Ceil(INST_BYTE_WIDTH).toInt          // 指令地址对齐的偏移量
  val START_ADDR: Long    = 0x00000084                               // 起始执行地址
  val SHAMT_WIDTH         = 5                                        // 位移量位宽
  val REG_NUMS            = 32                                       // 寄存器数量
  val REG_WIDTH           = log2Ceil(REG_NUMS).toInt                 // 寄存器号位数
  val REG_ZERO_HOME       = 0                                        // zero 寄存器的地址
  val REG_RA31_HOME       = 31                                       // RA寄存器地址为31
}
