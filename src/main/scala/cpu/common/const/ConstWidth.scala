package cpu.common.const

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
  val PC_WIDTH        = ADDR_WIDTH
  val REG_NUM         = 32
  val REG_WIDTH       = 5
  val CTRL_WIDTH      = 5
  val STRB_WIDTH      = 4
  val SRAM_SIZE_WIDTH = 2
}

trait ConstWidthAxi {
  val AXI_ADDR_WIDTH  = 32
  val AXI_DATA_WIDTH  = 32
  val AXI_ID_WIDTH    = 4
  val AXI_LEN_WIDTH   = 4
  val AXI_SIZE_WIDTH  = 3
  val AXI_BURST_WIDTH = 2
  val AXI_LOCK_WIDTH  = 2
  val AXI_CACHE_WIDTH = 4
  val AXI_PROT_WIDTH  = 3
  val AXI_STRB_WIDTH  = 4
  val AXI_RESP_WIDTH  = 2
  val INT_WIDTH       = 6
}
