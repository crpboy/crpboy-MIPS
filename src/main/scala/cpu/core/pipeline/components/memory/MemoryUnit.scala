package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._

import cpu.utils.Functions._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache = new Bundle { val sram_rdata = Input(UInt(DATA_WIDTH.W)) }
    val in     = Flipped(Decoupled(new StageExecuteMemory))
    val out    = Decoupled(new StageMemoryWriteback)
  })
  val input  = io.in.bits
  val output = io.out.bits

  val data = Mux(
    input.inst.fu === fu_mem && input.inst.wb,
    MuxLookup(
      input.inst.fuop,
      0.U,
      Seq(
        mem_lw -> io.dCache.sram_rdata,
      ),
    ),
    input.data,
  )

  output.data     := data
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc

  io.in.ready  := true.B
  io.out.valid := io.in.valid
}
