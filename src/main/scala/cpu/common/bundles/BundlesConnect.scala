package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

// pipeline stage bundle
class PipelineStage extends Bundle {
  val pc       = UInt(PC_WIDTH.W)
  val debug_pc = UInt(PC_WIDTH.W)
  val slot     = Bool()
  val exInfo   = new ExInfo
}
class StageFetchDecode extends PipelineStage {
  val inst = UInt(INST_WIDTH.W)
}
class StageDecodeExecute extends PipelineStage {
  val inst   = new InstInfoExt
  val op1    = UInt(DATA_WIDTH.W)
  val op2    = UInt(DATA_WIDTH.W)
  val rsaddr = UInt(REG_WIDTH.W)
  val rtaddr = UInt(REG_WIDTH.W)
}
class StageExecuteMemory extends PipelineStage {
  val inst       = new InstInfo
  val memReqInfo = new MemReqInfo
  val memByte    = UInt(2.W)
  val exSel      = UInt(3.W)
  val rsaddr     = UInt(REG_WIDTH.W)
  val rtaddr     = UInt(REG_WIDTH.W)
  val data       = UInt(DATA_WIDTH.W)
}
class StageMemoryWriteback extends PipelineStage {
  val inst   = new InstInfo
  val exSel  = UInt(3.W)
  val rsaddr = UInt(REG_WIDTH.W)
  val rtaddr = UInt(REG_WIDTH.W)
  val data   = UInt(DATA_WIDTH.W)
}
