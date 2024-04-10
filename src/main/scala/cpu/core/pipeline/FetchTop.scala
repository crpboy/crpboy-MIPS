package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._
import cpu.core.pipeline.components.fetch._

class FetchTopIO extends Bundle {
  val in  = Input(new StagePreFetch)
  val out = Output(new StageFetchDecode)
}

class FetchTop extends Module {
  val io      = IO(new FetchTopIO)
  val stage   = Module(new Stage(new StagePreFetch)).io
  val pc      = Module(new PC).io
  val memInst = Module(new MemInst).io

  stage.in <> io.in
  stage.out.en <> memInst.en
  stage.out.en <> pc.en
  stage.out.en <> io.out.en

  pc.pc <> memInst.pc
  memInst.inst <> io.out.inst
}
