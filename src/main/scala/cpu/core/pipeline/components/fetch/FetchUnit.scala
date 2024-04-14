package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline.components.fetch._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val debug = new DebugIO
    // val binfo = Input(new BranchInfo)
    val jinfo = Input(new JmpInfo)
    val out   = Decoupled(Output(new StageFetchDecode))
  })
  val pcNext = Module(new GenNextPC).io
  val pcReg  = RegInit(PC_INIT_ADDR.U(PC_WIDTH.W))
  val output = io.out.bits

  pcNext.en := true.B
  io.jinfo <> pcNext.jinfo
  io.debug.in.resetn <> pcNext.rst

  pcNext.in                   := pcReg
  pcReg                       := pcNext.out
  io.debug.out.inst_sram_addr := pcReg

  output.inst  := io.debug.in.inst_sram_rdata
  io.out.valid := true.B
}
