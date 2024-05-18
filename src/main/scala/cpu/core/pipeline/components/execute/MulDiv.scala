package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.const.Const._
import cpu.utils.Functions._
import cpu.common.bundles.{InstInfoExt, CtrlInfo}

class MulDiv extends Module {
  val io = IO(new Bundle {
    val inst  = Input(new InstInfoExt)
    val ctrl  = Input(new CtrlInfo)
    val op1   = Input(UInt(DATA_WIDTH.W))
    val op2   = Input(UInt(DATA_WIDTH.W))
    val block = Output(Bool())
    val wen   = Output(Bool())
    val wdata = Output(UInt(HILO_WIDTH.W))
  })
  val mul = Module(new Mul).io
  val div = Module(new Div).io

  val en       = io.inst.fu === fu_md
  val isSigned = io.inst.fuop === _md_signed && !io.ctrl.ex
  val ismul    = io.inst.fuop === _md_mul
  mul.en       := (en && ismul && !io.ctrl.ex)
  div.en       := (en && !ismul && !io.ctrl.ex)
  mul.isSigned := isSigned
  div.isSigned := isSigned

  mul.op1 <> io.op1
  mul.op2 <> io.op2
  div.op1 <> io.op1
  div.op2 <> io.op2

  val ready = Mux(ismul, mul.ready, div.ready)
  val data  = Mux(ismul, mul.wdata, div.wdata)
  io.block := en && !ready && !io.ctrl.ex
  io.wen   := en && ready
  io.wdata := data
}
