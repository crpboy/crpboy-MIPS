package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

class MulDiv extends Module {
  val io = IO(new Bundle {
    val inst  = Input(new InstInfoExt)
    val ctrl  = Input(new CtrlInfo)
    val rs    = Input(UInt(DATA_WIDTH.W))
    val rt    = Input(UInt(DATA_WIDTH.W))
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

  mul.rs <> io.rs
  mul.rt <> io.rt
  div.rs <> io.rs
  div.rt <> io.rt

  val ready = Mux(ismul, mul.ready, div.ready)
  val data  = Mux(ismul, mul.wdata, div.wdata)
  io.block := en && !ready && !io.ctrl.ex
  io.wen   := en && ready
  io.wdata := data
}
