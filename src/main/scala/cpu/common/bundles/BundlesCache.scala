package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

class ICacheIO extends Bundle {
  val valid     = Output(Bool())
  val pcNext    = Output(UInt(PC_WIDTH.W))
  val coreReady = Output(Bool())
  val data      = Input(UInt(DATA_WIDTH.W))
  val stall     = Input(Bool())
}

class DCacheIOExe extends Bundle {
  val valid     = Output(Bool())
  val wen       = Output(Bool())
  val addr      = Output(UInt(ADDR_WIDTH.W))
  val size      = Output(UInt(AXI_SIZE_WIDTH.W))
  val wstrb     = Output(UInt(AXI_STRB_WIDTH.W))
  val wdata     = Output(UInt(DATA_WIDTH.W))
  val coreReady = Output(Bool())
  val stall     = Input(Bool())
}
class DCacheIOMem extends Bundle {
  val data      = Input(UInt(DATA_WIDTH.W))
  val coreReady = Output(Bool())
  val stall     = Input(Bool())
}
class DCacheIO extends Bundle {
  val exe = new DCacheIOExe
  val mem = new DCacheIOMem
}
