package cpu.common.const

import chisel3._
import chisel3.util._

trait ConstWidth {
  // base def
  val ADDR_WIDTH      = 32
  val ADDR_INST_WIDTH = 32
  val DATA_WIDTH      = 32
  val BYTE_WIDTH      = 8
  val BYTE_NUM        = DATA_WIDTH / BYTE_WIDTH
  val INST_WIDTH      = 32
  val PC_WIDTH        = ADDR_WIDTH

  // top IO
  val DEBUG_WEN_WIDTH = 4
  val SRAM_SIZE_WIDTH = 2

  // reg
  val HILO_WIDTH = 64
  val REG_NUM    = 32
  val REG_WIDTH  = 5

  // branch predict
  val BPU_BHT_WIDTH   = 6
  val BPU_INDEX_WIDTH = 6

  // cache
  val CACHE_OFFSET_WIDTH    = 4
  val CACHE_INDEX_WIDTH     = 8
  val CACHE_TAG_WIDTH       = DATA_WIDTH - CACHE_INDEX_WIDTH - CACHE_OFFSET_WIDTH
  val CACHE_NO_OFFSET_WIDTH = DATA_WIDTH - CACHE_OFFSET_WIDTH

  val CACHE_WAY_WIDTH = 1
  val CACHE_WAY_NUM   = 1 << CACHE_WAY_WIDTH

  val CACHE_LINE_BYTE_NUM = 1 << CACHE_OFFSET_WIDTH
  val CACHE_LINE_WIDTH    = CACHE_LINE_BYTE_NUM * BYTE_WIDTH
  val CACHE_LINE_DEPTH    = 1 << CACHE_INDEX_WIDTH

  val CACHE_BANK_WIDTH = DATA_WIDTH
  val CACHE_BANK_NUM   = CACHE_LINE_WIDTH / CACHE_BANK_WIDTH

  val CACHE_BUFFER_DEPTH          = 2
  val CACHE_UNCACHED_BUFFER_DEPTH = 4

  // others
  val CTRL_WIDTH      = 5
  val TLB_INDEX_WIDTH = 4
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
