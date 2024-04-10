package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.utils._

class JumpCtrlIO extends Bundle {
  val inst = Input(new InstInfoEXE)
  val writePC = Input(Bool())
}

class JumpCtrl extends Module{
  val io = IO(new JumpCtrlIO)

}