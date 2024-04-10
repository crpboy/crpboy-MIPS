package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._

// register bundle
class RegAddrBundle extends Bundle {
  val rs = UInt(REG_WIDTH.W)
  val rt = UInt(REG_WIDTH.W)
}
class RegDataBundle extends Bundle {
  val rs = UInt(DATA_WIDTH.W)
  val rt = UInt(DATA_WIDTH.W)
}

// inst info
abstract class InstInfo extends Bundle {
  val wb = Bool()
  val rd = UInt(REG_WIDTH.W)
}
class InstInfoWB extends InstInfo {}
class InstInfoMEM extends InstInfoWB {
  val instType = UInt(INS_LEN.W)
}
class InstInfoEXE extends InstInfoMEM {
  val regAddr = new RegAddrBundle
  val imm     = UInt(DATA_WIDTH.W)
  val op1Type = UInt(OP_LEN.W)
  val op2Type = UInt(OP_LEN.W)
  val aluType = UInt(ALU_LEN.W)
  val srcType = UInt(SRC_LEN.W)
}
class WBInfo extends InstInfo {
  val data = UInt(DATA_WIDTH.W)
}

// test info
class DebugInfo extends Bundle {
  val en = Bool()
}

// pipeline stage bundle
class PipelineStage extends Bundle {
  val en = Bool()
}
class StagePreFetch extends PipelineStage {}
class StageFetchDecode extends PipelineStage {
  val inst = UInt(INST_WIDTH.W)
}
class StageDecodeExecute extends PipelineStage {
  val instInfo = new InstInfoEXE
}
class StageExecuteMemory extends PipelineStage {
  val instInfo = new InstInfoMEM
  val data     = UInt(DATA_WIDTH.W)
}
class StageMemoryWriteback extends PipelineStage {
  val instInfo = new InstInfoWB
  val data     = UInt(DATA_WIDTH.W)
}
