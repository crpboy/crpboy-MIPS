package cpu

import chisel3._
import chisel3.util._
import cpu.core.pipeline.components._
import cpu.common.Config._

class Top extends Module {
  val alu      = Module(new ALU)
  val ctrl     = Module(new Ctrl)
  val decoder  = Module(new Decoder)
  val memData  = Module(new MemData)
  val memInst  = Module(new MemInst)
  val pc       = Module(new PC)
  val register = Module(new Register)

  pc.io.out <> memInst.io.addrPC
  memInst.io.out <> decoder.io.inst
  decoder.io.out <> ctrl.io.inst
  register.io.data <> alu.io.data
}
