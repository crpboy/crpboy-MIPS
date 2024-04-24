package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.common.Config._
import cpu.utils.Functions._

class SignedMul extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val CLK = Input(Clock())
    val CE  = Input(Bool())
    val A   = Input(UInt((DATA_WIDTH + 1).W))
    val B   = Input(UInt((DATA_WIDTH + 1).W))
    val P   = Output(UInt((HILO_WIDTH + 2).W))
  })
}

class Mul extends Module {
  val io = IO(new Bundle {
    val en       = Input(Bool())
    val rs       = Input(UInt(DATA_WIDTH.W))
    val rt       = Input(UInt(DATA_WIDTH.W))
    val isSigned = Input(Bool())
    val ready    = Output(Bool())
    val wdata    = Output(UInt(HILO_WIDTH.W))
  })
  val mul = Module(new SignedMul).io

  val cnt = RegInit(0.U(log2Ceil(mulClockNum + 1).W))
  cnt := Mux(io.en && !io.ready, cnt + 1.U, 0.U)

  mul.CLK := clock
  mul.CE  := io.en
  mul.A := Mux(
    io.isSigned,
    signedExtend(io.rs, mul.A.getWidth),
    zeroExtend(io.rs, mul.A.getWidth),
  )
  mul.B := Mux(
    io.isSigned,
    signedExtend(io.rt, mul.B.getWidth),
    zeroExtend(io.rt, mul.B.getWidth),
  )

  io.ready := cnt >= mulClockNum.U
  io.wdata := mul.P(HILO_WIDTH - 1, 0)
}
