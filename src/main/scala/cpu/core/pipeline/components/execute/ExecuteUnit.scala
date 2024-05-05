package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

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
    val ctrl    = Input(new CtrlInfo)
    val rCp0    = Flipped(new ReadCp0Info)
    val fetch   = new Bundle { val isBr = Output(Bool()) }

    val in  = Input(new StageDecodeExecute)
    val out = Output(new StageExecuteMemory)
  })

  val alu    = Module(new ALU).io
  val muldiv = Module(new MulDiv).io
  val hilo   = Module(new Hilo).io
  val bra    = Module(new BranchCtrl).io
  val memReq = Module(new MemReq).io

  val input  = io.in
  val output = io.out
  val except = WireDefault(input.exInfo)

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
  io.ctrl          <> bra.ctrl
  bra.binfo.bwen   <> io.binfo.bwen
  bra.binfo.bwaddr <> io.binfo.bwaddr

  input.rs   <> muldiv.rs
  input.rt   <> muldiv.rt
  input.inst <> muldiv.inst
  io.ctrl    <> muldiv.ctrl

  input.rs     <> hilo.movdata
  input.inst   <> hilo.inst
  muldiv.wen   <> hilo.wen
  muldiv.wdata <> hilo.wdata
  io.ctrl      <> hilo.ctrl

  input.rs   <> memReq.rs
  input.rt   <> memReq.rt
  input.inst <> memReq.inst
  io.dCache  <> memReq.dCache
  io.ctrl    <> memReq.ctrl

  val cp0ismfc0 = input.inst.fuop === cp0_mfc0
  val cp0sel    = input.inst.imm(2, 0)
  io.rCp0.addr := input.inst.rd
  io.rCp0.sel  := cp0sel

  // data select
  val pcNext = input.pc + 4.U
  val data = MuxLookup(
    input.inst.fu,
    0.U,
    Seq(
      fu_alu -> alu.out,
      fu_mem -> input.rt,
      fu_cp0 -> Mux(
        cp0ismfc0,
        io.rCp0.data,
        input.rt,
      ),
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
  val rd = Mux(
    input.inst.fu === fu_cp0 && cp0ismfc0,
    input.rtaddr,
    input.inst.rd,
  )

  io.dHazard.wen    := input.inst.wb
  io.dHazard.waddr  := rd
  io.dHazard.wdata  := data
  io.dHazard.isload := input.inst.fu === fu_mem && input.inst.wb

  io.ctrlreq.clear       := false.B
  io.ctrlreq.block       := muldiv.block
  io.ctrlreq.branchPause := bra.binfo.bwen

  when(alu.ex) {
    except.en     := true.B
    except.excode := ex_Ov
  }.elsewhen(memReq.exLoad) {
    except.en       := true.B
    except.excode   := ex_AdEL
    except.badvaddr := memReq.badvaddr
  }.elsewhen(memReq.exStore) {
    except.en       := true.B
    except.excode   := ex_AdES
    except.badvaddr := memReq.badvaddr
  }

  io.fetch.isBr := input.inst.fu === fu_bra && io.binfo.bwen

  output.exInfo    := except
  output.slot      := input.slot
  output.exSel     := cp0sel
  output.data      := data
  output.rsaddr    := input.rsaddr
  output.rtaddr    := input.rtaddr
  output.memByte   := memReq.memByte
  output.inst.fu   := input.inst.fu
  output.inst.fuop := input.inst.fuop
  output.inst.rd   := rd
  output.inst.wb   := input.inst.wb
  output.pc        := input.pc
  output.debug_pc  := input.debug_pc
}
