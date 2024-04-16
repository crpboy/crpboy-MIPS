package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._

import cpu.utils._
import cpu.utils.Functions._
import cpu.common.Const._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache = new DCacheIO
    val in     = Flipped(Decoupled(Input(new StageExecuteMemory)))
    val out    = Decoupled(Output(new StageMemoryWriteback))
  })
  val input  = io.in.bits
  val output = io.out.bits

  val en  = input.inst.fu === fu_mem
  val wen = !input.inst.wb
  io.dCache.sram_en   := en
  io.dCache.sram_wen  := wen
  io.dCache.sram_addr := input.inst.rd
  io.dCache.sram_wdata := MuxLookup(
    input.inst.fuop,
    0.U,
    Seq(
      mem_sb -> zeroExtend(input.data(7, 0)),
      mem_sh -> zeroExtend(input.data(15, 0)),
      mem_sw -> input.data,
    ),
  ) // TODO: 信号处理未完成

  output.data        := Mux(en && input.inst.wb, io.dCache.sram_rdata, input.data)
  output.inst        := input.inst
  output.debug_wb_pc := input.debug_wb_pc

  io.in.ready  := true.B
  io.out.valid := true.B
}
