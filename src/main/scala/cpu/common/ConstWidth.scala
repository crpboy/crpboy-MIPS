package cpu.common

import chisel3._
import chisel3.util._
import math._

trait ConstWidth {
  val ADDR_WIDTH      = 32
  val ADDR_INST_WIDTH = 32
  val DATA_WIDTH      = 32
  val HILO_WIDTH      = 64
  val WEN_WIDTH       = 4
  val INST_WIDTH      = 32
  val PC_WIDTH        = 32
  val REG_NUM         = 32
  val REG_WIDTH       = 5
  val CTRL_WIDTH      = 5
  val CP0_WIDTH       = 32
}
