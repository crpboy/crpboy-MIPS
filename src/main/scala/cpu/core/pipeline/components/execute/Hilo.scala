package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class HiloIO extends Bundle {
  val en       = Input(Bool())
  val instType = Input(UInt(INS_LEN.W))
  val mulRes   = Input(UInt(HILO_WIDTH.W))
  val hiloSel  = Input(Bool())

  // val wbEn      = Input(Bool())
  // val wbData    = Input(UInt(HILO_WIDTH.W))
  // val wbHiloSel = Input(Bool())

  val hiRes = Output(UInt(DATA_WIDTH.W))
  val loRes = Output(UInt(DATA_WIDTH.W))
}

class Hilo extends Module {
  val io    = IO(new HiloIO)
  val hiReg = Reg(UInt(DATA_WIDTH.W))
  val loReg = Reg(UInt(DATA_WIDTH.W))
  when(io.en) {
    hiReg := io.mulRes(63,32)
    loReg := io.mulRes(31,0)
  }
  io.hiRes := hiReg
  io.loRes := loReg
}
