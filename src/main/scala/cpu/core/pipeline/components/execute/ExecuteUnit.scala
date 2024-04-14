package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline.components.execute._

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(Input(new StageDecodeExecute)))
    val out = Decoupled(Output(new StageExecuteMemory))
  })

  val alu    = Module(new ALU).io
  val hilo   = Module(new Hilo).io
  val mul    = Module(new MulDiv).io
  val input  = io.in.bits
  val output = io.out.bits

  input.inst <> alu.inst
  input.inst <> mul.inst
  input.data <> alu.data
  input.data <> mul.data

  hilo.wen <> mul.wen
  hilo.wdata <> mul.wdata

  output.data := MuxLookup(
    input.inst.fu,
    0.U,
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

  output.inst.fu   := input.inst.fu
  output.inst.fuop := input.inst.fuop
  output.inst.rd   := input.inst.rd
  output.inst.wb   := input.inst.wb

  io.in.ready  := true.B
  io.out.valid := true.B
}
