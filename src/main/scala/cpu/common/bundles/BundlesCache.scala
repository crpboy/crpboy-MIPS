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

class MemReqInfo extends Bundle {
  val valid = Bool()
  val wen   = Bool()
  val addr  = UInt(ADDR_WIDTH.W)
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val wstrb = UInt(AXI_STRB_WIDTH.W)
  val wdata = UInt(DATA_WIDTH.W)
}
class DCacheIOReq extends Bundle {
  val info = Output(new MemReqInfo)
}
class DCacheIOResp extends Bundle {
  val data = Input(UInt(DATA_WIDTH.W))
}
class DCacheIO extends Bundle {
  val req       = new DCacheIOReq
  val resp      = new DCacheIOResp
  val coreReady = Output(Bool())
  val stall     = Input(Bool())
}
