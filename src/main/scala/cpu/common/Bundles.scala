package cpu.common

import chisel3._
import chisel3.util._
import cpu.common.Const._

// inst info
class InstInfo extends Bundle {
  val wb   = Bool()
  val fu   = UInt(FU_LEN.W)
  val fuop = UInt(FUOP_LEN.W)
  val rd   = UInt(REG_WIDTH.W)
}
class InstInfoExt extends InstInfo {
  val op1 = UInt(OPR_LEN.W)
  val op2 = UInt(OPR_LEN.W)
  val imm = UInt(DATA_WIDTH.W)
}

// ctrl info
class CtrlInfo extends Bundle {
  val stall = Bool()
  val flush = Bool()
}
class CtrlRequest extends Bundle {
  val block = Bool()
  val clear = Bool()
}
class CtrlRequestExecute extends CtrlRequest {
  val branchPause = Bool()
}

// top IO
// inst
class ICacheIO extends Bundle {
  val sram_rdata = Input(UInt(INST_WIDTH.W))
  val sram_en    = Output(Bool())
  val sram_wen   = Output(UInt(WEN_WIDTH.W))
  val sram_addr  = Output(UInt(ADDR_WIDTH.W))
  val sram_wdata = Output(UInt(DATA_WIDTH.W))
}
// data
class DCacheIO extends Bundle {
  val sram_rdata = Input(UInt(DATA_WIDTH.W))
  val sram_en    = Output(Bool())
  val sram_wen   = Output(UInt(WEN_WIDTH.W))
  val sram_addr  = Output(UInt(ADDR_WIDTH.W))
  val sram_wdata = Output(UInt(DATA_WIDTH.W))
}
// debug info
class DebugIO extends Bundle {
  val wb_pc       = Output(UInt(PC_WIDTH.W))
  val wb_rf_wen   = Output(UInt(WEN_WIDTH.W))
  val wb_rf_wnum  = Output(UInt(REG_WIDTH.W))
  val wb_rf_wdata = Output(UInt(DATA_WIDTH.W))
}

// write back info
class JWBInfo extends Bundle {
  val wen   = Bool()
  val wdata = UInt(DATA_WIDTH.W)
}
class WBInfo extends Bundle {
  val wen   = Bool()
  val wdata = UInt(DATA_WIDTH.W)
  val waddr = UInt(REG_WIDTH.W)
}

// jump info
class JmpInfo extends Bundle {
  val jwen   = Bool()
  val jwaddr = UInt(PC_WIDTH.W)
}

// branch info
class BraInfo extends Bundle {
  val bwen   = Bool()
  val bwaddr = UInt(PC_WIDTH.W)
}

// hazard
class DataHazard extends Bundle {
  val wen   = Bool()
  val waddr = UInt(REG_WIDTH.W)
  val wdata = UInt(DATA_WIDTH.W)
}
class DataHazardExe extends DataHazard {
  val isload = Output(Bool())
}
