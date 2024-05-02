package cpu.common

import chisel3._
import chisel3.util._
import cpu.common.Const._

// pipeline stage bundle
class PipelineStage extends Bundle {
  val debug_pc = Output(UInt(PC_WIDTH.W))
  val pc       = Output(UInt(PC_WIDTH.W))
}
class StageFetchDecode extends PipelineStage {
  val inst = Output(UInt(INST_WIDTH.W))
}
class StageDecodeExecute extends PipelineStage {
  val inst   = Output(new InstInfoExt)
  val rs     = Output(UInt(DATA_WIDTH.W))
  val rt     = Output(UInt(DATA_WIDTH.W))
  val rsaddr = Output(UInt(REG_WIDTH.W))
  val rtaddr = Output(UInt(REG_WIDTH.W))
}
class StageExecuteMemory extends PipelineStage {
  val inst    = Output(new InstInfo)
  val memByte = Output(UInt(2.W))
  val data    = Output(UInt(DATA_WIDTH.W))
}
class StageMemoryWriteback extends PipelineStage {
  val inst = Output(new InstInfo)
  val data = Output(UInt(DATA_WIDTH.W))
}