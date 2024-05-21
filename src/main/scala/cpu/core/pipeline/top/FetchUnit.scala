package cpu.core.pipeline.top

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val iCache = new ICacheIO
    val tlb    = Flipped(new TlbSearchIO)

    val jinfo = Input(new JmpInfo)
    val binfo = Input(new BraInfo)
    val cp0 = Input(new Bundle {
      val isex    = Input(Bool())
      val exEntry = Input(UInt(EX_ENRTY_WIDTH.W))
      val eret    = Input(Bool())
      val eretpc  = Input(UInt(PC_WIDTH.W))
    })
    val slotSignal = Input(new Bundle {
      val decode  = Input(Bool())
      val execute = Input(Bool())
    })
    val ctrl = Input(new CtrlInfo)

    val ctrlreq = Output(new CtrlRequest)
    val out     = Decoupled(new StageFetchDecode)
  })
  val output = io.out.bits

  // gen pcNext
  val ctrlSignal = io.ctrl.stall || io.ctrl.ex
  val exSignal   = io.cp0.isex   || io.cp0.eret
  val pcReg = RegEnable(
    io.iCache.pcNext,
    (PC_INIT_ADDR_SUB.U)(PC_WIDTH.W),
    !io.iCache.stall && (!ctrlSignal || exSignal),
  )
  val pcNextTmp = pcReg + 4.U
  val pcNext = MuxCase(
    pcNextTmp,
    Seq(
      io.cp0.isex   -> EX_INIT_ADDR,
      io.cp0.eret   -> io.cp0.eretpc,
      io.jinfo.jwen -> io.jinfo.jwaddr,
      io.binfo.bwen -> io.binfo.bwaddr,
    ),
  )

  // <> tlb
  val instValid = !(reset.asBool)
  io.tlb.req.en     := instValid
  io.tlb.req.isLoad := true.B
  io.tlb.req.vaddr  := pcNext

  // tlb exception
  val except = WireDefault(0.U.asTypeOf(new ExInfo))
  when(io.tlb.exInfo.isTlbInvalid) {
    except.en     := true.B
    except.excode := ex_TLBL
  }
  when(io.tlb.exInfo.isTlbModified) {
    except.en     := true.B
    except.excode := ex_Mod
  }
  when(io.tlb.exInfo.isTlbRefill) {
    except.en     := true.B
    except.excode := ex_TLBL
    except.entry  := EXENTRY_TLB_REFILL
  }

  // <> iCache
  val resetTmp = RegNext(reset)
  io.iCache.pcNext    := io.tlb.paddr
  io.iCache.valid     := instValid
  io.iCache.coreReady := !io.ctrl.stall

  // stall flush ctrl
  io.ctrlreq.block := resetTmp
  io.ctrlreq.clear := false.B
  io.out.valid     := true.B

  output.slot     := io.slotSignal.decode || io.slotSignal.execute
  output.exInfo   := except
  output.inst     := Mux(io.ctrl.flush, 0.U, io.iCache.data)
  output.pc       := pcNextTmp
  output.debug_pc := pcReg
}
