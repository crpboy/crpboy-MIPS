package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.utils.Functions._
import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline.components.execute._

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val dCache = new Bundle {
      val sram_en    = Output(Bool())
      val sram_wen   = Output(UInt(WEN_WIDTH.W))
      val sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
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

  input.pc <> bra.pc

  hilo.wen <> mul.wen
  hilo.wdata <> mul.wdata

  bra.binfo.en <> io.binfo.en
  bra.binfo.bwen <> io.binfo.bwen
  bra.binfo.bwaddr <> io.binfo.bwaddr

  // data select
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
      fu_jmp -> (input.pc + 4.U),
      fu_bra -> (input.pc + 4.U),
      fu_mem -> input.rt,
    ),
  )

  // TODO: 信号处理未完成
  // mem request
  io.dCache.sram_en   := input.inst.fu === fu_mem
  io.dCache.sram_addr := input.rs + input.inst.imm
  io.dCache.sram_wen  := Mux(!input.inst.wb, WD_EN, WD_NO)
  io.dCache.sram_wdata := MuxLookup(
    input.inst.fuop,
    0.U,
    Seq(
      mem_sb -> zeroExtend(output.data(7, 0)),
      mem_sh -> zeroExtend(output.data(15, 0)),
      mem_sw -> output.data,
    ),
  )

  output.inst.fu   := input.inst.fu
  output.inst.fuop := input.inst.fuop
  output.inst.rd   := input.inst.rd
  output.inst.wb   := input.inst.wb
  output.pc        := input.pc
  output.debug_pc  := input.debug_pc

  io.in.ready  := true.B
  io.out.valid := io.in.valid
}
