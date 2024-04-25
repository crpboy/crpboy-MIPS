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

  val rdata = io.dCache.sram_rdata
  val lbres = MuxLookup(
    input.memByte,
    0.U,
    Seq(
      "b00".U -> signedExtend(rdata(7, 0)),
      "b01".U -> signedExtend(rdata(15, 8)),
      "b10".U -> signedExtend(rdata(23, 16)),
      "b11".U -> signedExtend(rdata(31, 24)),
    ),
  )
  val lbures = MuxLookup(
    input.memByte,
    0.U,
    Seq(
      "b00".U -> zeroExtend(rdata(7, 0)),
      "b01".U -> zeroExtend(rdata(15, 8)),
      "b10".U -> zeroExtend(rdata(23, 16)),
      "b11".U -> zeroExtend(rdata(31, 24)),
    ),
  )
  val word = Mux(
    !input.inst.fuop(2).asBool,
    rdata,
    MuxLookup(
      input.inst.fuop,
      0.U,
      Seq(
        mem_lb  -> lbres,
        mem_lbu -> lbures,
        mem_lh -> Mux(
          input.memByte(1).asBool,
          signedExtend(rdata(31, 16)),
          signedExtend(rdata(15, 0)),
        ),
        mem_lhu -> Mux(
          input.memByte(1).asBool,
          zeroExtend(rdata(31, 16)),
          zeroExtend(rdata(15, 0)),
        ),
      ),
    ),
  )
  val data = Mux(
    input.inst.fu === fu_mem && input.inst.wb,
    MuxLookup(
      input.inst.fuop,
      word,
      Seq(
        mem_lwl -> MuxLookup(
          input.memByte,
          0.U,
          Seq(
            "b00".U -> Cat(word(7, 0), input.data(23, 0)),
            "b01".U -> Cat(word(15, 0), input.data(15, 0)),
            "b10".U -> Cat(word(23, 0), input.data(7, 0)),
            "b11".U -> word,
          ),
        ),
        mem_lwr -> MuxLookup(
          input.memByte,
          0.U,
          Seq(
            "b00".U -> word,
            "b01".U -> Cat(input.data(31, 24), word(31, 8)),
            "b10".U -> Cat(input.data(31, 16), word(31, 16)),
            "b11".U -> Cat(input.data(31, 8), word(31, 24)),
          ),
        ),
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
