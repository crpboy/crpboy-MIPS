package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class Hilo extends Module {
  val io = IO(new Bundle {
    val wen     = Input(Bool())
    val wdata   = Input(UInt(HILO_WIDTH.W))
    val movdata = Input(UInt(DATA_WIDTH.W))
    val inst    = Input(new InstInfoExt)
    val ctrl    = Input(new CtrlInfo)
    val hi      = Output(UInt(DATA_WIDTH.W))
    val lo      = Output(UInt(DATA_WIDTH.W))
  })
  val reg = RegEnable(io.wdata, 0.U, io.wen)
  when(io.inst.fu === fu_mov && io.inst.fuop === _mov_ismt && !io.ctrl.ex) {
    reg := Mux(
      io.inst.fuop === _mov_usehi,
      Cat(io.movdata, reg(DATA_WIDTH - 1, 0)),
      Cat(reg(HILO_WIDTH - 1, DATA_WIDTH), io.movdata),
    )
  }
  io.hi := reg(HILO_WIDTH - 1, DATA_WIDTH)
  io.lo := reg(DATA_WIDTH - 1, 0)
}
