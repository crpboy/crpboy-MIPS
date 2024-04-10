package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class MulDivCalcIO extends Bundle {
  val en     = Input(Bool())
  val op1    = Input(UInt(DATA_WIDTH.W))
  val op2    = Input(UInt(DATA_WIDTH.W))
  // val readEn = Output(Bool())
  val res    = Output(UInt(HILO_WIDTH.W))
}

private class Mul extends Module {
  val io = IO(new MulDivCalcIO)
  io.res := io.op1 * io.op2 // TODO
}

private class Div extends Module {
  val io = IO(new MulDivCalcIO)
  io.res := io.op1 / io.op2 // TODO
}

class MulDivIO extends Bundle {
  val en      = Input(Bool())
  val aluType = Input(UInt(ALU_LEN.W))
  val regData = Input(new RegDataBundle)
  val res     = Output(UInt(HILO_WIDTH.W))
  val HiloWen     = Output(Bool())
}

class MulDiv extends Module {
  val io  = IO(new MulDivIO)
  val mul = Module(new Mul).io
  val div = Module(new Div).io

  val op1 = io.regData.rs
  val op2 = io.regData.rt
  mul.en  := io.aluType === alu_mul
  div.en  := io.aluType === alu_div
  mul.op1 := op1; mul.op2 := op2
  div.op1 := op1; div.op2 := op2

  val res = MuxLookup(
    io.aluType,
    0.U,
    Seq(
      alu_mul -> mul.res,
      alu_div -> div.res,
    ),
  )
  io.res := res
}
