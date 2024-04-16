package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.utils._

class GenNextPC extends Module {
  val io = IO(new Bundle {
    val in    = Input(UInt(PC_WIDTH.W)) // current pc
    val en    = Input(Bool())           // can gen next pc
    val jinfo = new JmpInfo             // jump info
    // val b   = new BranchInfo // branch info
    val out = Output(UInt(PC_WIDTH.W)) // next pc
  })
  io.out := MuxCase(
    Mux(io.en, io.in + PC_BYTE_WIDTH.U, io.in),
    Seq(
      io.jinfo.jwen -> io.jinfo.jwaddr,
      // io.b.bwen -> io.b.bwaddr,
    ),
  )
}
