package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common.Const._

class ExCtrl extends Module {
  val io = IO(new Bundle {
    val exID  = Input(Bool())
    val exEXE = Input(Bool())
    val exMEM = Input(Bool())
    val exWB  = Input(Bool())
    val out   = Output(UInt(CTRL_WIDTH.W))
  })
  val in  = Cat(io.exID, io.exEXE, io.exMEM, io.exWB).asUInt
  val out = Wire(Vec(5, Bool()))
  out(4) := false.B
  out(3) := in(2) | in(1) | in(0)
  out(2) := in(1) | in(0)
  out(1) := in(0)
  out(0) := false.B
  io.out := out.asUInt
}
