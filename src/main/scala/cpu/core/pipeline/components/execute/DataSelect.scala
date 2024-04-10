package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class DataSelectIO extends Bundle {
  val en       = Input(Bool())
  val instType = Input(UInt(INS_LEN.W))
  val aluType  = Input(UInt(ALU_LEN.W))
  val aluRes   = Input(UInt(DATA_WIDTH.W))
  // val hiRes    = Input(UInt(DATA_WIDTH.W))
  // val loRes    = Input(UInt(DATA_WIDTH.W))
  val rsData   = Input(UInt(DATA_WIDTH.W))
  val data     = Output(UInt(DATA_WIDTH.W))
}

class DataSelect extends Module {
  val io = IO(new DataSelectIO)
  val data = MuxLookup(
    io.aluType,
    0.U,
    Seq(
      alu_x   -> io.rsData,
      // alu_mul -> io.,
    ),
  )
  io.data := data
}
