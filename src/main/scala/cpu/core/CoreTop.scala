package cpu.core

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.utils._
import cpu.utils.StageConnect._

import pipeline.components.decode._
import pipeline.components.execute._
import pipeline.components.fetch._
import pipeline.components.memory._
import pipeline.components.writeback._
import os.write

class CoreTop extends Module {
  val io = IO(new Bundle {
    val debug = new DebugIO
  })
  val fetch     = Module(new FetchUnit).io
  val decode    = Module(new DecodeUnit).io
  val execute   = Module(new ExecuteUnit).io
  val memory    = Module(new MemoryUnit).io
  val writeback = Module(new WriteBackUnit).io

  stageConnect(fetch.out, decode.in)
  stageConnect(decode.out, execute.in)
  stageConnect(execute.out, memory.in)
  stageConnect(memory.out, writeback.in)

  io.debug <> fetch.debug
  fetch.jinfo <> decode.jinfo

  writeback.out.waddr <> decode.wb.waddr
  writeback.out.wdata <> decode.wb.wdata
  writeback.out.wen <> decode.wb.wen
}
