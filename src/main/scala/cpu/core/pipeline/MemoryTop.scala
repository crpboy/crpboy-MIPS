package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.core.pipeline.components.memory._
import cpu.utils._

class MemoryTopIO extends Bundle {
  val in  = Input(new StageExecuteMemory)
  val out = Output(new StageMemoryWriteback)
}

class MemoryTop extends Module {
  val io    = IO(new MemoryTopIO)
  val stage = Module(new Stage(new StageExecuteMemory)).io
  val mem   = Module(new MemData).io
  val sel   = Module(new MemSelect).io

  stage.in <> io.in
  stage.out.en <> mem.en
  stage.out.en <> sel.en
  stage.out.instInfo.rd <> io.out.instInfo.rd
  stage.out.instInfo.wb <> io.out.instInfo.wb

  stage.out.data <> mem.dataAddr
  stage.out.data <> sel.exeData
  stage.out.instInfo.instType <> sel.instType
  stage.out.instInfo.instType <> mem.instType

  mem.memData <> sel.memData

  io.out.en <> stage.out.en
  io.out.data <> sel.data
  io.out.instInfo.rd <> stage.out.instInfo.rd
  io.out.instInfo.wb <> stage.out.instInfo.wb
}
