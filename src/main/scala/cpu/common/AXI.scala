package cpu.common

import chisel3._
import chisel3.util._
import cpu.common.Const._

// all signals are defined for master(cpu, output)
trait AxiConst {
  val AXI_ADDR_WIDTH  = ADDR_WIDTH
  val AXI_DATA_WIDTH  = DATA_WIDTH
  val AXI_ID_WIDTH    = 4
  val AXI_LEN_WIDTH   = 4
  val AXI_SIZE_WIDTH  = 3
  val AXI_BURST_WIDTH = 2
  val AXI_LOCK_WIDTH  = 2
  val AXI_CACHE_WIDTH = 4
  val AXI_PORT_WIDTH  = 3
  val AXI_STRB_WIDTH  = 4
  val AXI_RESP_WIDTH  = 2
}
class AxiWriteAddr extends Bundle with AxiConst {
  val id    = UInt(AXI_ID_WIDTH.W)
  val addr  = UInt(AXI_ADDR_WIDTH.W)
  val len   = UInt(AXI_LEN_WIDTH.W)
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val burst = UInt(AXI_BURST_WIDTH.W)
  val lock  = UInt(AXI_LOCK_WIDTH.W)
  val cache = UInt(AXI_CACHE_WIDTH.W)
  val port  = UInt(AXI_PORT_WIDTH.W)
}
class AxiWriteData extends Bundle with AxiConst {
  val id   = UInt(AXI_ID_WIDTH.W)
  val data = UInt(AXI_DATA_WIDTH.W)
  val strb = UInt(AXI_STRB_WIDTH.W)
  val last = Bool()
}
class AxiWriteResp extends Bundle with AxiConst {
  val id   = UInt(AXI_ID_WIDTH.W)
  val resp = UInt(AXI_RESP_WIDTH.W)
}
class AxiReadAddr extends Bundle with AxiConst {
  val id    = UInt(AXI_ID_WIDTH.W)
  val addr  = UInt(AXI_ADDR_WIDTH.W)
  val len   = UInt(AXI_LEN_WIDTH.W)
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val burst = UInt(AXI_BURST_WIDTH.W)
  val lock  = UInt(AXI_LOCK_WIDTH.W)
  val cache = UInt(AXI_CACHE_WIDTH.W)
  val port  = UInt(AXI_PORT_WIDTH.W)
}
class AxiReadData extends Bundle with AxiConst {
  val id   = UInt(AXI_ID_WIDTH.W)
  val data = UInt(AXI_DATA_WIDTH.W)
  val strb = UInt(AXI_STRB_WIDTH.W)
}
class AxiLowPower extends Bundle with AxiConst {
  val sysreq = Bool()
  val sysack = Bool()
  val active = Bool()
}
class AXI extends Bundle {
  val aw = Decoupled(new AxiWriteAddr)
  val w  = Decoupled(new AxiWriteData)
  val b  = Decoupled(new AxiWriteResp)
  val ar = Decoupled(new AxiReadAddr)
  val r  = Decoupled(new AxiReadData)
}
