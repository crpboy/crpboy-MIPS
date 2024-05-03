package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class DecodeUnit extends Module {
  val io = IO(new Bundle {
    val wb          = Input(new WBInfo)
    val jinfo       = Output(new JmpInfo)
    val ctrlreq     = Output(new CtrlRequest)
    val slotForward = Output(Bool())
    val eretInfo    = Output(new Bundle { val en = Bool() })

    val exeDHazard = Input(new DataHazardExe)
    val memDHazard = Input(new DataHazard)
    val wbDHazard  = Input(new DataHazard)

    val ctrl = Input(new CtrlInfo)
    val in   = Input(new StageFetchDecode)
    val out  = Output(new StageDecodeExecute)
  })

  val decoder = Module(new Decoder).io
  val reg     = Module(new RegFile).io
  val jmp     = Module(new JumpCtrl).io

  val input  = io.in
  val output = io.out

  // data forward
  val rsdata = MuxCase(
    reg.rsdata,
    Seq(
      (io.exeDHazard.wen && io.exeDHazard.waddr === reg.rsaddr) -> io.exeDHazard.wdata,
      (io.memDHazard.wen && io.memDHazard.waddr === reg.rsaddr) -> io.memDHazard.wdata,
      (io.wbDHazard.wen && io.wbDHazard.waddr === reg.rsaddr)   -> io.wbDHazard.wdata,
    ),
  )
  val rtdata = MuxCase(
    reg.rtdata,
    Seq(
      (io.exeDHazard.wen && io.exeDHazard.waddr === reg.rtaddr) -> io.exeDHazard.wdata,
      (io.memDHazard.wen && io.memDHazard.waddr === reg.rtaddr) -> io.memDHazard.wdata,
      (io.wbDHazard.wen && io.wbDHazard.waddr === reg.rtaddr)   -> io.wbDHazard.wdata,
    ),
  )

  // decoder input
  input.inst <> decoder.rawInst
  input.pc   <> jmp.pc

  // reg
  decoder.rsaddr <> reg.rsaddr
  decoder.rtaddr <> reg.rtaddr
  io.wb          <> reg.wb

  // jump
  rsdata           <> jmp.regData
  decoder.instInfo <> jmp.inst
  io.ctrl          <> jmp.ctrl
  io.jinfo.jwen    := jmp.out.jwen
  io.jinfo.jwaddr  := jmp.out.jwaddr

  // except
  val except   = WireDefault(input.exInfo)
  val instInfo = decoder.instInfo
  val cp0en    = instInfo.fu === fu_cp0
  io.slotForward := instInfo.fu === fu_jmp || instInfo.fu === fu_bra
  when(cp0en) {
    when(instInfo.fuop === cp0_syscall) {
      except.en     := true.B
      except.excode := ex_Sys
    }
    when(instInfo.fuop === cp0_break) {
      except.en     := true.B
      except.excode := ex_Bp
    }
  }
  io.eretInfo.en := cp0en && instInfo.fuop === cp0_eret

  // control request
  io.ctrlreq.block := io.exeDHazard.isload &&
    (io.exeDHazard.waddr === reg.rsaddr ||
      io.exeDHazard.waddr === reg.rtaddr)
  io.ctrlreq.clear := except.en

  output.exInfo   := except
  output.inst     := decoder.instInfo
  output.rs       := rsdata
  output.rt       := rtdata
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
  output.rsaddr   := decoder.rsaddr
  output.rtaddr   := decoder.rtaddr
}
