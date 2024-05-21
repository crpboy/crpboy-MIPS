package cpu.core.pipeline.components.mmu

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class TLB extends Module with Config {
  val io = IO(new Bundle {
    val s = Vec(tlbSearchPortNum, new TlbSearchIO)
    val wr = new Bundle {
      val wen = Input(Bool())
      val in  = Input(new TlbInput)
      val out = Output(new TlbInfo)
    }
  })
  val output = io.wr.out
  val input  = io.wr.in

  val tlb = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U.asTypeOf(new TlbInfo))))

  for (searchId <- 0 to 1) {
    val s     = io.s(searchId)
    val vaddr = s.req.vaddr

    val res    = WireDefault(0.U.asTypeOf(new TlbSearchRes))
    val exInfo = WireDefault(0.U.asTypeOf(new TlbExInfo))
    val paddr  = WireDefault(0.U(ADDR_WIDTH.W))
    val mapped = vaddr < KSEG0_START_ADDR || vaddr >= KSEG23_START_ADDR

    when(mapped) {
      for (i <- 0 until TLB_NUM) {
        when(
          tlb(i).vpn2 === vaddr(31, 13) &&
            (tlb(i).g || tlb(i).asid === io.wr.in.entryHi.asid),
        ) {
          when(!vaddr(12)) {
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
          }.elsewhen(!res.d && !s.req.isLoad) {
            exInfo.isTlbModified := true.B
          }
          res.found := true.B
        }
        paddr := Cat(res.pfn(19, 0), vaddr(11, 0))
      }
      when(!res.found) { exInfo.isTlbRefill := true.B }
    }.otherwise {
      paddr := vaddr
    }
    s.exInfo := Mux(s.req.en, exInfo, 0.U.asTypeOf(exInfo))
    s.paddr  := paddr
  }

  // read & write
  val index = input.index.index
  when(io.wr.wen) {
    val wdata = WireDefault(0.U.asTypeOf(new TlbInfo))

    wdata.vpn2 := input.entryHi.vpn2
    wdata.asid := input.entryHi.asid

    wdata.g    := input.entryLo0.g0 | input.entryLo1.g1
    wdata.pfn0 := input.entryLo0.pfn0
    wdata.c0   := input.entryLo0.c0
    wdata.d0   := input.entryLo0.d0
    wdata.v0   := input.entryLo0.v0

    wdata.pfn1 := input.entryLo1.pfn1
    wdata.c1   := input.entryLo1.c1
    wdata.d1   := input.entryLo1.d1
    wdata.v1   := input.entryLo1.v1

    tlb(index) := wdata
  }
  output := tlb(index).asTypeOf(new TlbInfo)
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
