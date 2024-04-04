package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class CtrlIO extends Bundle {
  val inst = Input(new BundleInst)
  val aluUseImm = Output(Bool())
}

class Ctrl extends Module {
  val io = IO(new CtrlIO)
}