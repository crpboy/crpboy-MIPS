package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val iCache = new Bundle {
      val inst_sram_rdata = Input(UInt(INST_WIDTH.W))
      val inst_sram_en    = Output(Bool())
      val inst_sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val pcNext          = Output(UInt(PC_WIDTH.W))
    }
    val jinfo  = Input(new JmpInfo)
    val binfo  = Input(new BraInfo)
    val exInfo = Input(new ExInfoWB)
    val ctrl   = Input(new CtrlInfo)
    val isSlot = Input(Bool())

    val ctrlreq = Output(new CtrlRequest)
    val out     = Output(new StageFetchDecode)
  })
  val output = io.out

  val pcReg      = RegNext(io.iCache.pcNext, (PC_INIT_ADDR_SUB.U)(PC_WIDTH.W))
  val pcNextTmp  = pcReg + 4.U
  val ctrlSignal = io.ctrl.stall || io.ctrl.flush || io.ctrl.ex
  val pcNext = MuxCase(
    pcNextTmp,
    Seq(
      ctrlSignal     -> pcReg,
      io.jinfo.jwen  -> io.jinfo.jwaddr,
      io.binfo.bwen  -> io.binfo.bwaddr,
      io.exInfo.eret -> io.exInfo.pc,
      io.exInfo.en   -> EX_INIT_ADDR.U,
    ),
  )
  val rst = RegNext(reset)
  io.iCache.pcNext         := pcNext
  io.iCache.inst_sram_addr := io.iCache.pcNext
  io.iCache.inst_sram_en   := !(reset.asBool)

  val except = WireDefault(0.U.asTypeOf(new ExInfo))
  except.slot := io.isSlot

  io.ctrlreq.block := rst
  io.ctrlreq.clear := false.B

  output.exInfo   := except
  output.inst     := io.iCache.inst_sram_rdata
  output.pc       := pcNextTmp
  output.debug_pc := pcReg
}
