package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.bundles._
import cpu.common.const.Const._

class RegFile extends Module {
  val io = IO(new Bundle {
    val wb     = Input(new WBInfo)
    val rsaddr = Input(UInt(REG_WIDTH.W))
    val rtaddr = Input(UInt(REG_WIDTH.W))
    val rsdata = Output(UInt(DATA_WIDTH.W))
    val rtdata = Output(UInt(DATA_WIDTH.W))
  })
  val reg = RegInit(VecInit(Seq.fill(REG_NUM)(0.U(DATA_WIDTH.W))))
  io.rsdata := reg(io.rsaddr)
  io.rtdata := reg(io.rtaddr)
  when(io.wb.wen) {
    reg(io.wb.waddr) := io.wb.wdata
  }
}
