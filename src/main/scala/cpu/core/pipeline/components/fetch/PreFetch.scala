package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class PreFetch extends Module {
  val io = IO(new Bundle {
    val iCache = new ICacheIO
    val pcNext = Input(UInt(PC_WIDTH.W))
    val data   = Decoupled(UInt(INST_WIDTH.W))
  })
  io.iCache.sram_en    := !(reset.asBool)
  io.iCache.sram_addr  := io.pcNext
  io.iCache.sram_wen   := 0.U
  io.iCache.sram_wdata := 0.U

  val resetTmp = RegNext(reset.asBool)
  io.data.bits  := io.iCache.sram_rdata
  io.data.valid := !resetTmp
}
