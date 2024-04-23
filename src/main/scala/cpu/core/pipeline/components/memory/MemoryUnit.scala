package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._

import cpu.utils.Functions._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.memory._

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val dCache  = new Bundle { val sram_rdata = Input(UInt(DATA_WIDTH.W)) }
    val dHazard = Output(new DataHazard)
    val in      = new KeepFlushIO(new StageExecuteMemory)
    val out     = new StageMemoryWriteback
    val ctrlreq = Output(new CtrlRequest)
  })

  val input   = io.in.bits
  val output  = io.out
  val ctrlreq = WireInit(0.U.asTypeOf(new CtrlRequest))
  ctrlreq <> io.ctrlreq

  val data = Mux(
    input.inst.fu === fu_mem && input.inst.wb,
    MuxLookup(
      input.inst.fuop,
      input.data,
      Seq(
        mem_lw -> io.dCache.sram_rdata,
      ),
    ),
    input.data,
  )

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := data

  output.data     := data
  output.inst     := input.inst
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
}
