package cpu.core.pipeline.components.mmu

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class TLB extends Module with Config {
  val io = IO(new Bundle {
    val wen = Input(Bool())
    val s   = Vec(tlbSearchPortNum, new TlbSearchIO)
    val in  = Input(new TlbInput)
    val out = Output(new TlbInfo)
  })
  val tlb = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U.asTypeOf(new TlbInfo))))

  for (searchId <- 0 to 1) {
    val s      = io.s(searchId)
    val res    = WireDefault(0.U.asTypeOf(new TlbSearchRes))
    val exInfo = WireDefault(0.U.asTypeOf(new TlbExInfo))
    val found  = WireDefault(false.B)
    for (i <- 0 until TLB_NUM) {
      when(tlb(i).vpn2 === s.vpn2 && (tlb(i).g || tlb(i).asid === io.in.entryHi.asid)) {
        when(!s.oddPage) {
          res.pfn := tlb(i).pfn0
          res.v   := tlb(i).v0
          res.c   := tlb(i).c0
          res.d   := tlb(i).d0
        }.otherwise {
          res.pfn := tlb(i).pfn1
          res.v   := tlb(i).v1
          res.c   := tlb(i).c1
          res.d   := tlb(i).d1
        }
        when(!res.v) {
          exInfo.isTlbInvalid := true.B
        }
        when(!res.d && !s.isLoad) {
          exInfo.isTlbModified := true.B
        }
        found := true.B
      }
    }
    when(!found) {
      exInfo.isTlbRefill := true.B
    }
  }

  // read & write
  io.out := tlb(io.in.index.index).asTypeOf(new TlbInfo)
  when(io.wen) {
    val wdata = WireDefault(0.U.asTypeOf(new TlbInfo))

    wdata.vpn2 := io.in.entryHi.vpn2
    wdata.asid := io.in.entryHi.asid

    wdata.g    := io.in.entryLo0.g0 | io.in.entryLo1.g1
    wdata.pfn0 := io.in.entryLo0.pfn0
    wdata.c0   := io.in.entryLo0.c0
    wdata.d0   := io.in.entryLo0.d0
    wdata.v0   := io.in.entryLo0.v0

    wdata.pfn1 := io.in.entryLo1.pfn1
    wdata.c1   := io.in.entryLo1.c1
    wdata.d1   := io.in.entryLo1.d1
    wdata.v1   := io.in.entryLo1.v1

    tlb(io.in.index.index) := wdata
  }
}

/*

found ← 0
for i in 0...TLBEntries-1
  if (TLB[i].VPN2 = va31..13) and (TLB[i].G or (TLB[i].ASID = EntryHi.ASID)) then
    if va12 = 0 then
      pfn ← TLB[i].PFN0
      v ← TLB[i].V0
      c ← TLB[i].C0
      d ← TLB[i].D0
    else
      pfn ← TLB[i].PFN1
      v ← TLB[i].V1
      c ← TLB[i].C1
      d ← TLB[i].D1
    endif

    if v = 0 then
      SignalException(TLBInvalid, reftype)
    endif

    if (d = 0) and (reftype = store) then
      SignalException(TLBModified)
    endif

    # pfn19..0 corresponds to pa31..12
    pa ← pfn19..0 || va11..0
    found ← 1
    break

  endif
endfor

if found = 0 then
  SignalException(TLBMiss, reftype)
endif

 */

/*
  assign match0[ 0] = (s0_vpn2==tlb_vpn2[ 0]) && ((s0_asid==tlb_asid[ 0]) || tlb_g[ 0]);
  assign match0[ 1] = (s0_vpn2==tlb_vpn2[ 1]) && ((s0_asid==tlb_asid[ 1]) || tlb_g[ 1]);
  assign match0[15] = (s0_vpn2==tlb_vpn2[15]) && ((s0_asid==tlb_asid[15]) || tlb_g[15]);
  assign match1[ 0] = (s1_vpn2== tlb_vpn2[ 0]) && ((s1_asid==tlb_asid[ 0]) || tlb_g[ 0]);
  assign match1[ 1] = (s1_vpn2== tlb_vpn2[ 1]) && ((s1_asid==tlb_asid[ 1]) || tlb_g[ 1]);
  assign match1[15] = (s1_vpn2== tlb_vpn2[15]) && ((s1_asid==tlb_asid[15]) || tlb_g[15]);
 */
