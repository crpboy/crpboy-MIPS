package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class TlbInput extends Bundle {
  val index    = new Cp0IndexBundle
  val entryLo0 = new Cp0EntryLo0Bundle
  val entryLo1 = new Cp0EntryLo1Bundle
  val entryHi  = new Cp0EntryHiBundle
}
class TlbInfo extends Bundle {
  val vpn2 = UInt(TLB_VPN2_WIDTH.W)
  val asid = UInt(TLB_ASID_WIDTH.W)

  val g = Bool()

  val pfn0 = UInt(TLB_PFN_WIDTH.W)
  val c0   = UInt(TLB_C_WIDTH.W)
  val d0   = Bool()
  val v0   = Bool()

  val pfn1 = UInt(TLB_PFN_WIDTH.W)
  val c1   = UInt(TLB_C_WIDTH.W)
  val d1   = Bool()
  val v1   = Bool()
}

class TlbSearchRes extends Bundle {
  val found = Bool()
  val index = UInt(TLB_INDEX_WIDTH.W)
  val pfn   = UInt(TLB_PFN_WIDTH.W)
  val c     = UInt(TLB_C_WIDTH.W)
  val d     = Bool()
  val v     = Bool()
}
class TlbExInfo extends Bundle {
  val isTlbRefill   = Bool()
  val isTlbInvalid  = Bool()
  val isTlbModified = Bool()
}
class TlbSearchIO extends Bundle {
  val vpn2    = Input(UInt(TLB_VPN2_WIDTH.W))
  val oddPage = Input(Bool())
  val asid    = Input(UInt(TLB_ASID_WIDTH.W))
  val isLoad  = Input(Bool())
  val res     = Output(new TlbSearchRes)
  val exInfo  = Output(new TlbExInfo)
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

/*

input [ 18:0] s1_vpn2,
input s1_odd_page,
input [ 7:0] s1_asid,
output s1_found,
output [$clog2(16)-1:0] s1_index,
output [ 19:0] s1_pfn,
output [ 2:0] s1_c,
output s1_d,
output s1_v,

 */
