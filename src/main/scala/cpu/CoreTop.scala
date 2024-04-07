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
  // decoder <> memData
  decoder.io.instReadMem <> memData.io.instReadMem
  // decoder <> register
  decoder.io.instOp1Type <> register.io.instOp1Type
  decoder.io.instOp2Type <> register.io.instOp2Type
  decoder.io.regAddr     <> register.io.regAddr
  decoder.io.instSrcType <> register.io.instSrcType
  // decoder <> alu
  decoder.io.instOp1Type <> alu.io.instOp1Type
  decoder.io.instOp2Type <> alu.io.instOp2Type
  decoder.io.instAluType <> alu.io.instAluType
  decoder.io.imm         <> alu.io.imm

  // register
  register.io.regData <> alu.io.regData

  // alu
  alu.io.aluRes <> register.io.aluRes
  alu.io.aluRes <> memData.io.memDataAddr

  // memData
  memData.io.memDataRes <> register.io.memDataRes
}
