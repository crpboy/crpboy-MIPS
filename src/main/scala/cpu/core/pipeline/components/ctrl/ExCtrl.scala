package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class ExCtrl extends Module {
  val io = IO(new Bundle {
    val exID  = Input(new ExInfo)
    val exEXE = Input(new ExInfo)
    val exMEM = Input(new ExInfo)
    val exWB  = Input(new ExInfo)
    val out   = Output(UInt(CTRL_WIDTH.W))
  })
  val ex = Cat(
    io.exID.en,
    io.exEXE.en,
    io.exMEM.en,
    io.exWB.en,
  )
  val eret = Cat(
    io.exID.eret,
    io.exEXE.eret,
    io.exMEM.eret,
    io.exWB.eret,
  )
  val out = Wire(Vec(5, Bool()))
  out(4) := ex(3) | ex(2)   | ex(1)   | ex(0)   | eret(3) | eret(2) | eret(1) | eret(0)
  out(3) := ex(2) | ex(1)   | ex(0)   | eret(3) | eret(2) | eret(1) | eret(0)
  out(2) := ex(1) | ex(0)   | eret(2) | eret(1) | eret(0)
  out(1) := ex(0) | eret(1) | eret(0)
  out(0) := eret(0)
  io.out := out.asUInt
}
