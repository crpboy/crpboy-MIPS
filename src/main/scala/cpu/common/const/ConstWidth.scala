package cpu.common.const

import chisel3._
import chisel3.util._

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
  val EX_ENRTY_WIDTH  = 1
}

trait ConstWidthTlb {
  // Index
  val TLB_INDEX_WIDTH = 4

  // EntryHi
  val TLB_VPN2_WIDTH = 19
  val TLB_ASID_WIDTH = 8

  // EntryLo
  val TLB_PFN_WIDTH = 20
  val TLB_C_WIDTH   = 3

  // other defines
  val TLB_NUM = 1 << TLB_INDEX_WIDTH
}

/*

input we, // w(rite) e(nable)
input [$clog2(TLBNUM)-1:0] w_index,
input [ 18:0] w_vpn2,
input [ 7:0] w_asid,
input w_g,
input [ 19:0] w_pfn0,
input [ 2:0] w_c0,
input w_d0,
input w_v0,
input [ 19:0] w_pfn1,
input [ 2:0] w_c1,
input w_d1,
input w_v1,

 */
