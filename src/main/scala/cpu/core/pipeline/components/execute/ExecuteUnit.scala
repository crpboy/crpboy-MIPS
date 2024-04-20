package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline.components.execute._
import scala.meta.internal.tokens.BranchNamerMacros

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val in    = Flipped(Decoupled(new StageDecodeExecute))
    val out   = Decoupled(new StageExecuteMemory)
    val binfo = Output(new BraInfo)
  })

  val alu    = Module(new ALU).io
  val hilo   = Module(new Hilo).io
  val mul    = Module(new MulDiv).io
  val bra    = Module(new BranchCtrl).io
  val input  = io.in.bits
  val output = io.out.bits

  input.rs <> alu.rs
  input.rt <> alu.rt
  input.inst <> alu.inst

  input.rs <> mul.rs
  input.rt <> mul.rt
  input.inst <> mul.inst

  input.rs <> bra.rs
  input.rt <> bra.rt
  input.inst <> bra.inst

  input.debug_wb_pc <> bra.pc

  hilo.wen <> mul.wen
  hilo.wdata <> mul.wdata

  bra.binfo.en <> io.binfo.en
  bra.binfo.bwen <> io.binfo.bwen
  bra.binfo.bwaddr <> io.binfo.bwaddr

  output.data := MuxLookup(
    input.inst.fu,
    input.inst.imm,
    Seq(
      fu_alu -> alu.out,
      fu_mov -> MuxLookup(
        input.inst.fuop,
        0.U,
        Seq(
          mov_mfhi -> hilo.out(63, 32),
          mov_mflo -> hilo.out(31, 0),
        ),
      ),
    ),
  )

  output.inst.fu     := input.inst.fu
  output.inst.fuop   := input.inst.fuop
  output.inst.rd     := input.inst.rd
  output.inst.wb     := input.inst.wb
  output.debug_wb_pc := input.debug_wb_pc

  io.in.ready  := true.B
  io.out.valid := io.in.valid
}
