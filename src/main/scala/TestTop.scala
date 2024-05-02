
import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.StageConnect._

class Unit1Unit2Stage extends Bundle {
  val data = UInt(DATA_WIDTH.W)
  val C    = UInt(DATA_WIDTH.W)
}

class Unit1 extends Module {
  val io = IO(new Bundle {
    val A   = Input(UInt(DATA_WIDTH.W))
    val B   = Input(UInt(DATA_WIDTH.W))
    val C   = Input(UInt(DATA_WIDTH.W))
    val out = Output(new Unit1Unit2Stage)
  })
  io.out.C    := io.C
  io.out.data := io.A + io.B
}

class Unit2 extends Module {
  val io = IO(new Bundle {
    val in  = Input(new Unit1Unit2Stage)
    val out = Output(UInt(DATA_WIDTH.W))
  })
  io.out := io.in.data + io.in.C
}

class TestTop extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new CtrlInfo)
    val A    = Input(UInt(DATA_WIDTH.W))
    val B    = Input(UInt(DATA_WIDTH.W))
    val C    = Input(UInt(DATA_WIDTH.W))
    val ans  = Output(UInt(DATA_WIDTH.W))
  })
  val u1 = Module(new Unit1).io
  val u2 = Module(new Unit2).io
  u1.A <> io.A
  u1.B <> io.B
  u1.C <> io.C
  stageConnect(u1.out, u2.in, io.ctrl)
  io.ans := u2.out
}
