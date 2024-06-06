package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

// ICache IO
class ICacheIO extends Bundle {
  val uncached  = Output(Bool())
  val unmappped = Output(Bool())
  val valid     = Output(Bool())
  val addr      = Output(UInt(PC_WIDTH.W))
  val coreReady = Output(Bool())
  val data      = Input(UInt(DATA_WIDTH.W))
  val stall     = Input(Bool())
}

// dCache IO
class MemPreReqInfo extends Bundle {
  val valid = Bool()
  val wen   = Bool()
  val addr  = UInt(ADDR_WIDTH.W)
}
class MemReqInfo extends MemPreReqInfo {
  val size  = UInt(AXI_SIZE_WIDTH.W)
  val wstrb = UInt(AXI_STRB_WIDTH.W)
  val wdata = UInt(DATA_WIDTH.W)
}
class DCacheIOMem extends Bundle {
  val req       = Output(new MemReqInfo)
  val respData  = Input(UInt(DATA_WIDTH.W))
  val coreReady = Output(Bool())
  val stall     = Input(Bool())
}
class DCacheIOExe extends Bundle {
  val req   = Output(new MemPreReqInfo)
  val stall = Input(Bool())
}
class DCacheIO extends Bundle {
  val execute = new DCacheIOExe
  val memory  = new DCacheIOMem
}

// cacheline data
class CacheLine extends Bundle {
  val bits = Vec(CACHE_BANK_NUM, UInt(DATA_WIDTH.W))
  def read(id: UInt): UInt = {
    bits(id)
  }
  def write(id: UInt, value: UInt): Unit = {
    bits(id) := value
    val valueWidth = value.getWidth
  }
}

// sram <> dCache
class CacheSramReadInfo extends Bundle {
  val index = UInt(CACHE_INDEX_WIDTH.W)
}
class CacheSramWriteInfo extends Bundle {
  val index = UInt(CACHE_INDEX_WIDTH.W)
  val valid = Bool()
  val wen   = Bool()
  val data  = UInt(CACHE_LINE_WIDTH.W)
  val tag   = UInt(CACHE_TAG_WIDTH.W)
  val strb  = UInt(CACHE_LINE_BYTE_NUM.W)
  val dirt  = Bool()
}

// sram <> iCache
class ICacheSramInfo extends Bundle {
  val index = UInt(CACHE_INDEX_WIDTH.W)
  val valid = Bool()
  val wen   = Bool()
  val data  = UInt(CACHE_LINE_WIDTH.W)
  val tag   = UInt(CACHE_TAG_WIDTH.W)
}
class CacheSramResult extends Bundle {
  val data  = UInt(CACHE_LINE_WIDTH.W)
  val tag   = UInt(CACHE_TAG_WIDTH.W)
  val valid = Bool()
  val hit   = Bool()
}
class DCacheSramResult extends CacheSramResult {
  val dirty = Bool()
}
