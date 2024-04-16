package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Const._

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

// top IO
// inst IO
class ICacheIO extends Bundle {
  val sram_rdata = Input(UInt(INST_WIDTH.W))
  val sram_en    = Output(Bool())
  val sram_wen   = Output(UInt(WEN_WIDTH.W))
  val sram_addr  = Output(UInt(ADDR_WIDTH.W))
  val sram_wdata = Output(UInt(DATA_WIDTH.W))
}
// data IO
class DCacheIO extends Bundle {
  val sram_rdata = Input(UInt(DATA_WIDTH.W))
  val sram_en    = Output(Bool())
  val sram_wen   = Output(UInt(WEN_WIDTH.W))
  val sram_addr  = Output(UInt(ADDR_WIDTH.W))
  val sram_wdata = Output(UInt(DATA_WIDTH.W))
}
// debug info
class DebugInfo extends Bundle {
  val wb_pc       = UInt(PC_WIDTH.W)
  val wb_rf_wen   = UInt(WEN_WIDTH.W)
  val wb_rf_wnum  = UInt(REG_WIDTH.W)
  val wb_rd_wdata = UInt(DATA_WIDTH.W)
}

// pipeline stage bundle
class PipelineStage extends Bundle {
  val debug_wb_pc = UInt(PC_WIDTH.W)
}
class StageFetchDecode extends PipelineStage {
  val inst = UInt(INST_WIDTH.W)
}
class StageDecodeExecute extends PipelineStage {
  val inst = new InstInfoExt
  val rs   = UInt(DATA_WIDTH.W)
  val rt   = UInt(DATA_WIDTH.W)
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
