package cpu.core

import chisel3._
import chisel3.util._
import cpu.config.Config._

import cpu.utils._

class PCregIO extends Bundle {
  val bundleCtrl = Flipped(new BundleCtrl)
  val addrJump   = Input(UInt(ADDR_WIDTH.W)) // the target of jump
  val addrBranch = Input(UInt(ADDR_WIDTH.W)) // the target of branch
  val addrPC = Output(UInt(ADDR_WIDTH.W)) // current pc address
}

class PCreg extends Module {
  val io = IO(new PCregIO())
  val pc = RegInit(START_ADDR.U(ADDR_WIDTH.W))

  when(io.bundleCtrl.ctrlJump) {
    pc := io.addrJump
  }.elsewhen(io.bundleCtrl.ctrlBranch) {
    pc := io.addrBranch
  }.otherwise {
    pc := pc + ADDR_BYTE_WIDTH.U; // pc += 4
  }
  io.addrPC := pc
}
