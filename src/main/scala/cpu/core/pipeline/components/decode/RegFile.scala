package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._

class RegFile extends Module {
  val io = IO(new Bundle {
    val raddr = Input(new RegAddr)
    val wb    = new WBInfo
    val data  = Output(new RegData)
  })
  val reg = Reg(Vec(REG_NUM, UInt(DATA_WIDTH.W)))
  io.data.rs := reg(io.raddr.rs)
  io.data.rt := reg(io.raddr.rt)
  when(io.wb.wen) {
    reg(io.wb.waddr) := io.wb.wdata
  }
}
