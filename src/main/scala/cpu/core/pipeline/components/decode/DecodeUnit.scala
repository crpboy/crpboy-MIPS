package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._
import cpu.core.pipeline.components.decode._

class DecodeUnit extends Module {
  val io = IO(new Bundle {
    val in    = Flipped(Decoupled(new StageFetchDecode))
    val out   = Decoupled(new StageDecodeExecute)
    val wb    = Input(new WBInfo)
    val jinfo = Output(new JmpInfo)
  })

  val decoder = Module(new Decoder).io
  val reg     = Module(new RegFile).io
  val jmp     = Module(new JumpCtrl).io

  val input  = io.in.bits
  val output = io.out.bits

  input.inst <> decoder.rawInst
  decoder.rsaddr <> reg.rsaddr
  decoder.rtaddr <> reg.rtaddr
  io.wb <> reg.wb

  decoder.instInfo <> jmp.inst
  io.jinfo.jwen   := jmp.out.jwen && io.out.valid
  io.jinfo.jwaddr := jmp.out.jwaddr

  output.inst        := decoder.instInfo
  output.rs          := reg.rsdata
  output.rt          := reg.rtdata
  output.debug_wb_pc := input.debug_wb_pc

  io.in.ready  := true.B
  io.out.valid := io.in.valid
}
