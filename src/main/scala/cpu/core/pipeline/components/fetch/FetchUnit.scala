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
    }

    // val binfo = Input(new BranchInfo)
    val jinfo = Input(new JmpInfo)
    val out   = Decoupled(Output(new StageFetchDecode))
  })
  val pcNext = Module(new GenNextPC).io
  val pcReg  = RegInit(PC_INIT_ADDR.U(PC_WIDTH.W))
  val output = io.out.bits

  pcNext.en := true.B
  io.jinfo <> pcNext.jinfo

  pcNext.in                := pcReg
  pcReg                    := pcNext.out
  io.iCache.inst_sram_addr := pcReg

  output.inst        := io.iCache.inst_sram_rdata
  output.debug_wb_pc := pcReg

  io.out.valid           := true.B
  io.iCache.inst_sram_en := true.B
}
