package cpu.common

import chisel3._
import chisel3.util._
import math._

object Config {

  val ADDR_WIDTH          = 32                                        // 地址位宽
  val ADDR_BYTE_WIDTH     = ADDR_WIDTH / 8                            // 地址位宽按字节算
  val DATA_WIDTH          = 32                                        // 数据位宽
  val DECODE_WRA_WIDTH    = 2                                         // DECODE 存寄存器的地址类型
  val DECODE_IMM_WIDTH    = 3                                         // DECODE 立即数类型
  val OP_COUNT            = 77                                        // DECODE 指令个数
  val DECODE_INST_WIDTH   = ceil(log(OP_COUNT) / log(2)).toInt        // DECODE 指令类型
  val DECODE_SRC_WIDTH    = 4                                         // DECODE 指令数据来源类型，表示存寄存器的数据源
  val IMM_WIDTH           = 16                                        // 立即数位宽
  val INST_WIDTH          = 32                                        // 指令位宽
  val INST_HOME           = "src/test/scala/instruction/memInst.hex"  // 指令测试文件路径
  val INST_BYTE_WIDTH     = INST_WIDTH / 8                            // 指令位宽按字节算
  val INST_BYTE_WIDTH_LOG = ceil(log(INST_BYTE_WIDTH) / log(2)).toInt // 指令地址对齐的偏移量
  val MEM_INST_SIZE       = 1024                                      // 指令内存大小
  val MEM_DATA_SIZE       = 1024                                      // 数据内存大小
  val START_ADDR: Long    = 0x00000084                                // 起始执行地址
  val SHAMT_WIDTH         = 5                                         // 位移量位宽
  val REG_NUMS            = 32                                        // 寄存器数量
  val REG_WIDTH           = ceil(log(REG_NUMS) / log(2)).toInt        // 寄存器号位数
  val REG_RA31_ADDR       = 31                                        // RA寄存器地址为31
}
