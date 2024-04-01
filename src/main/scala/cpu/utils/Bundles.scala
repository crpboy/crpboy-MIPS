package cpu.utils

import chisel3._
import chisel3.util._
import cpu.config.Config._

class BundleReg extends Bundle {
  val rd = Output(UInt(REG_WIDTH.W)) // register destination
  val rs = Output(UInt(REG_WIDTH.W)) // register source 1
  val rt = Output(UInt(REG_WIDTH.W)) // register source 2
}

class BundleCtrl extends Bundle {
  val ctrlJump   = Output(Bool()) // jump
  val ctrlBranch = Output(Bool()) // branch
}
