package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val iCache = new ICacheIO
    val jinfo  = Input(new JmpInfo)
    val binfo  = Input(new BraInfo)
    val cp0 = Input(new Bundle {
      val isex   = Input(Bool())
      val eret   = Input(Bool())
      val eretpc = Input(UInt(PC_WIDTH.W))
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

  val pcReg      = RegNext(io.iCache.pcNext, (PC_INIT_ADDR_SUB.U)(PC_WIDTH.W))
  val pcNextTmp  = pcReg + 4.U
  val ctrlSignal = io.ctrl.stall || io.ctrl.ex
  val pcNext = MuxCase(
    pcNextTmp,
    Seq(
      io.cp0.isex   -> EX_INIT_ADDR.U,
      io.cp0.eret   -> io.cp0.eretpc,
      ctrlSignal    -> pcReg,
      io.jinfo.jwen -> io.jinfo.jwaddr,
      io.binfo.bwen -> io.binfo.bwaddr,
    ),
  )
  val resetTmp = RegNext(reset)
  io.iCache.pcNext    := pcNext
  io.iCache.valid     := !(reset.asBool)
  io.iCache.coreReady := !io.ctrl.stall

  val except = WireDefault(0.U.asTypeOf(new ExInfo))

  io.ctrlreq.block := resetTmp
  io.ctrlreq.clear := false.B
  io.out.valid     := true.B

  output.slot     := io.slotSignal.decode || io.slotSignal.execute
  output.exInfo   := except
  output.inst     := io.iCache.data
  output.pc       := pcNextTmp
  output.debug_pc := pcReg
}
