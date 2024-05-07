package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val preFetch = new Bundle {
      val pcNext = Output(UInt(PC_WIDTH.W))
      val data   = Flipped(Decoupled(UInt(INST_WIDTH.W)))
    }
    val jinfo = Input(new JmpInfo)
    val binfo = Input(new BraInfo)
    val cp0 = Input(new Bundle {
      val isex   = Input(Bool())
      val eret   = Input(Bool())
      val eretpc = Input(UInt(PC_WIDTH.W))
    })
    val slotSignal = Input(new Bundle {
      val decode  = Input(Bool())
      val execute = Input(Bool())
    })
    val ctrl    = Input(new CtrlInfo)
    val ctrlreq = Output(new CtrlRequest)
    val out     = Output(new StageFetchDecode)
  })
  val decInput = io.preFetch.data
  val output   = io.out

  // gen next pc
  val pcReg     = RegNext(io.preFetch.pcNext, (PC_INIT_ADDR_SUB.U)(PC_WIDTH.W))
  val pcNextTmp = pcReg + 4.U
  val dontgo    = !decInput.fire || io.ctrl.stall || io.ctrl.ex
  io.preFetch.pcNext := MuxCase(
    pcNextTmp,
    Seq(
      io.cp0.isex   -> EX_INIT_ADDR.U,
      io.cp0.eret   -> io.cp0.eretpc,
      dontgo        -> pcReg,
      io.jinfo.jwen -> io.jinfo.jwaddr,
      io.binfo.bwen -> io.binfo.bwaddr,
    ),
  )

  decInput.ready   := true.B
  io.ctrlreq.block := false.B
  io.ctrlreq.clear := false.B

  output.slot     := io.slotSignal.decode || io.slotSignal.execute
  output.exInfo   := WireDefault(0.U.asTypeOf(new ExInfo))
  output.inst     := Mux(io.preFetch.data.fire, io.preFetch.data.bits, 0.U)
  output.pc       := pcNextTmp
  output.debug_pc := pcReg
}
