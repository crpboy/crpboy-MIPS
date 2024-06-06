package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

// basic define
// aw (master) output
class AxiWriteAddr extends Bundle {
  val id    = UInt(AXI_ID_WIDTH.W)
  val addr  = UInt(AXI_ADDR_WIDTH.W)
  val len   = UInt(AXI_LEN_WIDTH.W)
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val burst = UInt(AXI_BURST_WIDTH.W)
  val lock  = UInt(AXI_LOCK_WIDTH.W)
  val cache = UInt(AXI_CACHE_WIDTH.W)
  val prot  = UInt(AXI_PROT_WIDTH.W)
}
// w (master) output
class AxiWriteData extends Bundle {
  val id   = UInt(AXI_ID_WIDTH.W)
  val data = UInt(AXI_DATA_WIDTH.W)
  val strb = UInt(AXI_STRB_WIDTH.W)
  val last = Bool()
}
// b (slave) input
class AxiWriteResp extends Bundle {
  val id   = UInt(AXI_ID_WIDTH.W)
  val resp = UInt(AXI_RESP_WIDTH.W)
}
// ar (master) output
class AxiReadAddr extends Bundle {
  val id    = UInt(AXI_ID_WIDTH.W)
  val addr  = UInt(AXI_ADDR_WIDTH.W)
  val len   = UInt(AXI_LEN_WIDTH.W)
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val burst = UInt(AXI_BURST_WIDTH.W)
  val lock  = UInt(AXI_LOCK_WIDTH.W)
  val cache = UInt(AXI_CACHE_WIDTH.W)
  val prot  = UInt(AXI_PROT_WIDTH.W)
}
// r (slave) input
class AxiReadData extends Bundle {
  val id   = UInt(AXI_ID_WIDTH.W)
  val data = UInt(AXI_DATA_WIDTH.W)
  val resp = UInt(AXI_RESP_WIDTH.W)
  val last = Bool()
}

// cache <> ram
class AXI extends Bundle {
  val aw = Decoupled(new AxiWriteAddr)
  val w  = Decoupled(new AxiWriteData)
  val b  = Flipped(Decoupled(new AxiWriteResp))
  val ar = Decoupled(new AxiReadAddr)
  val r  = Flipped(Decoupled(new AxiReadData))
}
class AXIRead extends Bundle {
  val ar = Decoupled(new AxiReadAddr)
  val r  = Flipped(Decoupled(new AxiReadData))
}
class AXIWrite extends Bundle {
  val aw = Decoupled(new AxiWriteAddr)
  val w  = Decoupled(new AxiWriteData)
  val b  = Flipped(Decoupled(new AxiWriteResp))
}

// debug
class DebugIO extends Bundle {
  val wb_pc       = Output(UInt(PC_WIDTH.W))
  val wb_rf_wen   = Output(UInt(DEBUG_WEN_WIDTH.W))
  val wb_rf_wnum  = Output(UInt(REG_WIDTH.W))
  val wb_rf_wdata = Output(UInt(DATA_WIDTH.W))
}
