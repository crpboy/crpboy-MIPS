package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.core.pipeline.components.decode._
import cpu.common._

class DecodeUnit extends Module {
  val io = IO(new Bundle {
    val wb    = Input(new WBInfo)
    val jinfo = Output(new JmpInfo)

    val exeDHazard = Input(new DataHazardExe)
    val memDHazard = Input(new DataHazard)
    val wbDHazard  = Input(new DataHazard)

    val in      = new KeepFlushIO(new StageFetchDecode)
    val out     = new StageDecodeExecute
    val ctrlreq = Output(new CtrlRequest)
  })

  val decoder = Module(new Decoder).io
  val reg     = Module(new RegFile).io
  val jmp     = Module(new JumpCtrl).io

  val input  = io.in.bits
  val output = io.out

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

  input.inst <> decoder.rawInst
  input.pc   <> jmp.pc

  decoder.rsaddr <> reg.rsaddr
  decoder.rtaddr <> reg.rtaddr
  io.wb          <> reg.wb

  rsdata           <> jmp.regData
  decoder.instInfo <> jmp.inst
  io.jinfo.jwen    := jmp.out.jwen
  io.jinfo.jwaddr  := jmp.out.jwaddr

  io.ctrlreq.keep := MuxCase(
    "b00000".U,
    Seq(
      io.exeDHazard.isload -> "b11000".U,
    ),
  )
  io.ctrlreq.flush := MuxCase(
    "b00000".U,
    Seq(
      io.exeDHazard.isload -> "b00100".U,
    ),
  )

  output.inst     := decoder.instInfo
  output.rs       := rsdata
  output.rt       := rtdata
  output.pc       := input.pc
  output.debug_pc := input.debug_pc
  output.rsaddr   := decoder.rsaddr
  output.rtaddr   := decoder.rtaddr
}
