package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class MemSelectIO extends Bundle {
  val en       = Input(Bool())
  val instType = Input(UInt(INS_LEN.W))
  val memData  = Input(UInt(DATA_WIDTH.W))
  val exeData  = Input(UInt(DATA_WIDTH.W))
  val data     = Output(UInt(DATA_WIDTH.W))
}

class MemSelect extends Module {
  val io   = IO(new MemSelectIO)
  val data = Mux(io.instType === inst_lw, io.memData, io.exeData)
  io.data := data
}
