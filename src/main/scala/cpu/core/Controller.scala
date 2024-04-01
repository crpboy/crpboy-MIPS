package cpu.core

import chisel3._
import chisel3.util._
import cpu.config.Config._

class ControllerIO extends Bundle {
  val ctrlJump = Output(Bool())
  val ctrlBranch = Output(Bool())
}

class Controller extends Module {
  val io = IO(new ControllerIO())
}
