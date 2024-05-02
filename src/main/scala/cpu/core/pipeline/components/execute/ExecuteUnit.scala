package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.core.pipeline.components.execute._

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val dCache = new Bundle {
      val sram_en    = Output(Bool())
      val sram_wen   = Output(UInt(WEN_WIDTH.W))
      val sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
    val binfo   = Output(new BraInfo)
    val dHazard = Output(new DataHazardExe)
    val ctrlreq = Output(new CtrlRequestExecute)

    val ctrl = Input(new CtrlInfo)
    val in   = Input(new StageDecodeExecute)
    val out  = Output(new StageExecuteMemory)
  })

  val alu    = Module(new ALU).io
  val muldiv = Module(new MulDiv).io
  val hilo   = Module(new Hilo).io
  val bra    = Module(new BranchCtrl).io
  val memReq = Module(new MemReq).io
  val cp0    = Module(new CP0).io

  val input  = io.in
  val output = io.out

  input.rs   <> alu.rs
  input.rt   <> alu.rt
  input.inst <> alu.inst

  input.rs   <> muldiv.rs
  input.rt   <> muldiv.rt
  input.inst <> muldiv.inst

  input.rs         <> bra.rs
  input.rt         <> bra.rt
  input.inst       <> bra.inst
  input.pc         <> bra.pc
  bra.binfo.bwen   <> io.binfo.bwen
  bra.binfo.bwaddr <> io.binfo.bwaddr

  input.rs   <> muldiv.rs
  input.rt   <> muldiv.rt
  input.inst <> muldiv.inst

  input.rs     <> hilo.movdata
  input.inst   <> hilo.inst
  muldiv.wen   <> hilo.wen
  muldiv.wdata <> hilo.wdata

  input.rs   <> memReq.rs
  input.rt   <> memReq.rt
  input.inst <> memReq.inst
  io.dCache  <> memReq.dCache

  // data select
  val pcNext = input.pc + 4.U
  val data = MuxLookup(
    input.inst.fu,
    0.U,
    Seq(
      fu_alu -> alu.out,
      fu_mem -> input.rt,
      fu_mov -> MuxLookup(
        input.inst.fuop,
        0.U,
        Seq(
          mov_mfhi -> hilo.hi,
          mov_mflo -> hilo.lo,
        ),
      ),
      fu_jmp -> pcNext,
      fu_bra -> pcNext,
    ),
  )

  io.dHazard.wen    := input.inst.wb
  io.dHazard.waddr  := input.inst.rd
  io.dHazard.wdata  := data
  io.dHazard.isload := input.inst.fu === fu_mem && input.inst.wb

  io.ctrlreq.clear       := alu.ex
  io.ctrlreq.block       := muldiv.block
  io.ctrlreq.branchPause := bra.binfo.bwen

  cp0 := DontCare

  output.data      := data
  output.memByte   := memReq.memByte
  output.inst.fu   := input.inst.fu
  output.inst.fuop := input.inst.fuop
  output.inst.rd   := input.inst.rd
  output.inst.wb   := input.inst.wb
  output.pc        := input.pc
  output.debug_pc  := input.debug_pc
}
