package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Const._

// register bundle
class RegAddr extends Bundle {
  val rs = UInt(REG_WIDTH.W)
  val rt = UInt(REG_WIDTH.W)
}
class RegData extends Bundle {
  val rs = UInt(DATA_WIDTH.W)
  val rt = UInt(DATA_WIDTH.W)
}

// inst info
class InstInfo extends Bundle {
  val wb   = Bool()
  val fu   = UInt(FUOP_LEN.W)
  val fuop = UInt(FU_LEN.W)
  val rd   = UInt(REG_WIDTH.W)

}
class InstInfoExt extends InstInfo {
  val op1 = UInt(OPR_LEN.W)
  val op2 = UInt(OPR_LEN.W)
  val imm = UInt(DATA_WIDTH.W)
}

// soc info
class DebugInput extends Bundle {
  // val clk             = Bool()
  val resetn          = Bool()
  val inst_sram_rdata = UInt(INST_WIDTH.W)
  // val data_sram_rdata = UInt(DATA_WIDTH.W)
}

class DebugOutput extends Bundle {
  // val inst_sram_en    = Bool()
  // val inst_sram_wen   = UInt(WEN_WIDTH.W)
  val inst_sram_addr = UInt(ADDR_WIDTH.W)
  // val inst_sram_wdata = UInt(DATA_WIDTH.W)

  // val data_sram_en    = Bool()
  // val data_sram_wen   = UInt(WEN_WIDTH.W)
  // val data_sram_addr  = UInt(ADDR_WIDTH.W)
  // val data_sram_wdata = UInt(DATA_WIDTH.W)

  // val debug_wb_pc       = UInt(PC_WIDTH.W)
  // val debug_wb_rf_wen   = UInt(WEN_WIDTH.W)
  // val debug_wb_rf_wnum  = UInt(REG_WIDTH.W)
  // val debug_wb_rd_wdata = UInt(DATA_WIDTH.W)
}

class DebugIO extends Bundle {
  val in  = Input(new DebugInput)
  val out = Output(new DebugOutput)
}

// pipeline stage bundle
class PipelineStage extends Bundle {}
class StageFetchDecode extends PipelineStage {
  val inst = UInt(INST_WIDTH.W)
}
class StageDecodeExecute extends PipelineStage {
  val inst = new InstInfoExt
  val data = new RegData
}
class StageExecuteMemory extends PipelineStage {
  val inst = new InstInfo
  val data = UInt(DATA_WIDTH.W)
}
class StageMemoryWriteback extends PipelineStage {
  val inst = new InstInfo
  val data = UInt(DATA_WIDTH.W)
}

// write back info
class WBInfo extends Bundle {
  val wen   = Input(Bool())
  val wdata = Input(UInt(DATA_WIDTH.W))
  val waddr = Input(UInt(REG_WIDTH.W))
}

// jump info
class JmpInfo extends Bundle {
  val jwen   = Input(Bool())
  val jwaddr = Input(UInt(PC_WIDTH.W))
}

// branch info
class BranchInfo extends Bundle {
  val bwen   = Input(Bool())
  val bwaddr = Input(UInt(PC_WIDTH.W))
}
