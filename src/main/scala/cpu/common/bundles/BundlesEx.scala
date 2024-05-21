package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

// ex info
class ExInfo extends Bundle {
  val en       = Bool()
  val slot     = Bool()
  val eret     = Bool()
  val excode   = UInt(EX_LEN.W)
  val badvaddr = UInt(ADDR_WIDTH.W)
  val pc       = UInt(PC_WIDTH.W)
  val entry    = UInt(EX_ENRTY_WIDTH.W)
}

// cp0 <> exe unit
class WriteCp0Info extends Bundle {
  val en   = Input(Bool())
  val data = Input(UInt(DATA_WIDTH.W))
  val addr = Input(UInt(REG_WIDTH.W))
  val sel  = Input(UInt(3.W))
}
class ReadCp0Info extends Bundle {
  val addr = Input(UInt(REG_WIDTH.W))
  val sel  = Input(UInt(3.W))
  val data = Output(UInt(DATA_WIDTH.W))
}
