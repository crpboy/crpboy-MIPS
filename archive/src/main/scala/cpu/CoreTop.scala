package cpu

import chisel3._
import chisel3.util._
import cpu.core.pipeline.components._
import cpu.common.Config._

class CoreTopIO extends Bundle {}

class CoreTop extends Module {
  val io = IO(new CoreTopIO)

  val alu      = Module(new ALU)
  val decoder  = Module(new Decoder)
  val memData  = Module(new MemData)
  val memInst  = Module(new MemInst)
  val pc       = Module(new PC)
  val register = Module(new Register)

  // pc
  pc.io.pc <> memInst.io.pc

  // memInst
  memInst.io.inst <> decoder.io.inst

  // decoder
  decoder.io.out.srcType <> memData.io.srcType

  decoder.io.out.regAddr <> register.io.regAddr
  decoder.io.out.writeReg <> register.io.writeReg
  decoder.io.out.srcType <> register.io.srcType

  decoder.io.out.op1Type <> alu.io.op1Type
  decoder.io.out.op2Type <> alu.io.op2Type
  decoder.io.out.instType <> alu.io.instType
  decoder.io.out.imm <> alu.io.imm
  decoder.io.out.shamt <> alu.io.shamt

  // register
  register.io.regData <> alu.io.regData

  // alu
  alu.io.aluRes <> register.io.aluRes
  alu.io.aluRes <> memData.io.memDataAddr

  // memData
  memData.io.memDataRes <> register.io.memDataRes

  // memData.io <> decoder.io
  // memData.io <> register.io
}
