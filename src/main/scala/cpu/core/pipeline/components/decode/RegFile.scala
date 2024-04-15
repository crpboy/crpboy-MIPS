package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._

class RegFile extends Module {
  val io = IO(new Bundle {
    val rsaddr = Input(UInt(REG_WIDTH.W))
    val rtaddr = Input(UInt(REG_WIDTH.W))
    val wb     = new WBInfo
    val rsdata = Output(UInt(DATA_WIDTH.W))
    val rtdata = Output(UInt(DATA_WIDTH.W))
  })
  val reg = Reg(Vec(REG_NUM, UInt(DATA_WIDTH.W)))
  io.rsdata := reg(io.rsaddr)
  io.rtdata := reg(io.rtaddr)
  when(io.wb.wen) {
    reg(io.wb.waddr) := io.wb.wdata
  }
}
