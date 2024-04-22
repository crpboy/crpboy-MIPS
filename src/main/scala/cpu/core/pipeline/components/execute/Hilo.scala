package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.utils.Functions._

class Hilo extends Module {
  val io = IO(new Bundle {
    val wen   = Input(Bool())
    val wdata = Input(UInt(DATA_WIDTH.W))
    val out   = Output(UInt(HILO_WIDTH.W))
  })
  val reg = RegInit(0.U(DATA_WIDTH.W))
  when(io.wen) {
    reg := io.wdata
  }
  io.out := reg
}
