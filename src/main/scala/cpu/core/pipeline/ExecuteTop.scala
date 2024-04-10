package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._
import cpu.core.pipeline.components.execute._

class ExecuteTopIO extends Bundle {
  val in     = Input(new StageDecodeExecute)
  val wbInfo = Input(new WBInfo)
  val out    = Output(new StageExecuteMemory)
}

class ExecuteTop extends Module {
  val io    = IO(new ExecuteTopIO)
  val stage = Module(new Stage(new StageDecodeExecute)).io
  val alu   = Module(new ALU).io
  val reg   = Module(new GPRs).io
  val sel = Module(new DataSelect).io
  // val md    = Module(new MulDiv).io // TODO
  // val hilo  = Module(new Hilo).io // TODO

  io.in <> stage.in
  io.wbInfo <> reg.wbInfo

  val en = stage.out.en
  stage.out.en <> io.out.en
  stage.out.en <> alu.en
  stage.out.en <> reg.en
  stage.out.en <> sel.en
  // stage.out.en <> md.en

  val inst = stage.out.instInfo
  inst.op1Type <> alu.op1Type
  inst.op2Type <> alu.op2Type
  inst.instType <> alu.instType
  inst.aluType <> alu.aluType
  inst.imm <> alu.imm

  inst.regAddr <> reg.regAddr

  inst.instType <> sel.instType
  inst.aluType <> sel.aluType
  alu.aluRes <> sel.aluRes

  reg.regData.rs <> sel.rsData
  reg.regData <> alu.regData

  io.out.en <> stage.out.en
  io.out.data <> sel.data
  io.out.instInfo.instType <> stage.out.instInfo.instType
  io.out.instInfo.wb <> stage.out.instInfo.wb
  io.out.instInfo.rd <> stage.out.instInfo.rd
}
