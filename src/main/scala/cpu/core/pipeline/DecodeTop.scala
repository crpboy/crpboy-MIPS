package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._
import cpu.core.pipeline.components.decode._

class DecodeTopIO extends Bundle {
  val in  = Input(new StageFetchDecode)
  val out = Output(new StageDecodeExecute)
}

class DecodeTop extends Module {
  val io      = IO(new DecodeTopIO)
  val stage   = Module(new Stage(new StageFetchDecode)).io
  val decoder = Module(new Decoder).io

  stage.in <> io.in
  stage.out.en <> decoder.en
  stage.out.en <> io.out.en

  io.in <> stage.in
  stage.out.inst <> decoder.rawInst
  decoder.instInfo <> io.out.instInfo
}
