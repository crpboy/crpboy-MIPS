package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.utils._

class MulDiv extends Module {
  val io = IO(new Bundle {
    val inst  = Input(new InstInfoExt)
    val rs    = Input(UInt(DATA_WIDTH.W))
    val rt    = Input(UInt(DATA_WIDTH.W))
    val wen   = Output(Bool())
    val wdata = Output(UInt(HILO_WIDTH.W))
  })
  // TODO: 需要添加乘除法模块
  val en = io.inst.fu === fu_mul
  io.wen := en
  io.wdata := Mux(
    en,
    MuxLookup(
      io.inst.fuop,
      0.U,
      Seq(
        md_mult  -> (io.rs.asSInt * io.rt.asSInt).asUInt,
        md_multu -> io.rs * io.rt,
      ),
    ),
    0.U,
  )
}
