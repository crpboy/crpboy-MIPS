package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline.components.fetch._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val iCache = new Bundle {
      val inst_sram_rdata = Input(UInt(INST_WIDTH.W))
      val inst_sram_en    = Output(Bool())
      val inst_sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val pcNext          = Output(UInt(PC_WIDTH.W))
    }
    val jinfo = Input(new JmpInfo)
    val binfo = Input(new BraInfo)
    val out   = Decoupled(new StageFetchDecode)
  })

  val preDecoder = Module(new PreDecoder).io
  val output     = io.out.bits

  val pcReg = RegNext(io.iCache.pcNext, ("hbfbffffc".U)(PC_WIDTH.W))
  // val pcDebug = RegNext(pcReg)
  val brReg = RegInit(false.B) // is in jump / branch

  io.iCache.inst_sram_rdata <> preDecoder.inst
  preDecoder.done := io.binfo.en

  when(preDecoder.done) {
    brReg := false.B
  }.elsewhen(preDecoder.isbr) {
    brReg := true.B
  }

  val isjmp = preDecoder.isbr && brReg
  val pcSelfInc = Mux(isjmp, pcReg, pcReg + 4.U)
  val pcNext = MuxCase(
    pcSelfInc,
    Seq(
      io.jinfo.jwen -> io.jinfo.jwaddr,
      io.binfo.bwen -> io.binfo.bwaddr,
    ),
  )
  io.iCache.pcNext         := pcNext
  io.iCache.inst_sram_addr := io.iCache.pcNext
  io.iCache.inst_sram_en   := io.out.valid & !this.reset.asBool

  output.inst        := io.iCache.inst_sram_rdata
  output.debug_wb_pc := pcReg

  io.out.valid := !brReg || preDecoder.done
}
