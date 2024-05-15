package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
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

class Mul extends Module with Config {
  val io = IO(new Bundle {
    val en       = Input(Bool())
    val op1      = Input(UInt(DATA_WIDTH.W))
    val op2      = Input(UInt(DATA_WIDTH.W))
    val isSigned = Input(Bool())

    val ready = Output(Bool())
    val wdata = Output(UInt(HILO_WIDTH.W))
  })
  if (isBuild) {
    val mul = Module(new SignedMul).io
    val cnt = RegInit(0.U(log2Ceil(mulClockNum + 1).W))
    cnt := Mux(io.en && !io.ready, cnt + 1.U, 0.U)

    mul.CLK := clock
    mul.CE  := io.en
    mul.A := Mux(
      io.isSigned,
      signedExtend(io.op1, mul.A.getWidth),
      zeroExtend(io.op1, mul.A.getWidth),
    )
    mul.B := Mux(
      io.isSigned,
      signedExtend(io.op2, mul.B.getWidth),
      zeroExtend(io.op2, mul.B.getWidth),
    )

    io.ready := cnt >= mulClockNum.U
    io.wdata := mul.P(HILO_WIDTH - 1, 0)
  } else {
    val cnt = RegInit(0.U(log2Ceil(mulClockNum + 1).W))
    cnt := Mux(io.en && !io.ready, cnt + 1.U, 0.U)
    val signed   = RegInit(0.U(HILO_WIDTH.W))
    val unsigned = RegInit(0.U(HILO_WIDTH.W))
    when(io.en) {
      signed   := (io.op1.asSInt * io.op2.asSInt).asUInt
      unsigned := io.op1 * io.op2
    }
    io.ready := cnt >= mulClockNum.U
    io.wdata := Mux(io.isSigned, signed, unsigned)
  }
}
