package cpu

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.core.pipeline._

class TopIO extends Bundle {
  val inst  = new ICacheIO
  val data  = new DCacheIO
  val debug = new DebugIO
}

class mycpu_top extends Module {
  val io = FlatIO(new Bundle {
    val inst_sram_rdata = Input(UInt(INST_WIDTH.W))
    val inst_sram_en    = Output(Bool())
    val inst_sram_wen   = Output(UInt(WEN_WIDTH.W))
    val inst_sram_addr  = Output(UInt(ADDR_WIDTH.W))
    val inst_sram_wdata = Output(UInt(DATA_WIDTH.W))

    val data_sram_rdata = Input(UInt(DATA_WIDTH.W))
    val data_sram_en    = Output(Bool())
    val data_sram_wen   = Output(UInt(WEN_WIDTH.W))
    val data_sram_addr  = Output(UInt(ADDR_WIDTH.W))
    val data_sram_wdata = Output(UInt(DATA_WIDTH.W))

    val debug_wb_pc       = Output(UInt(PC_WIDTH.W))
    val debug_wb_rf_wen   = Output(UInt(WEN_WIDTH.W))
    val debug_wb_rf_wnum  = Output(UInt(REG_WIDTH.W))
    val debug_wb_rf_wdata = Output(UInt(DATA_WIDTH.W))
  })
  val core   = Module(new CoreTop)
  val rstTmp = !reset.asBool
  core.reset <> rstTmp

  core.io.iCache.sram_rdata <> io.inst_sram_rdata
  core.io.iCache.sram_en    <> io.inst_sram_en
  core.io.iCache.sram_wen   <> io.inst_sram_wen
  core.io.iCache.sram_addr  <> io.inst_sram_addr
  core.io.iCache.sram_wdata <> io.inst_sram_wdata

  core.io.dCache.sram_rdata <> io.data_sram_rdata
  core.io.dCache.sram_en    <> io.data_sram_en
  core.io.dCache.sram_wen   <> io.data_sram_wen
  core.io.dCache.sram_addr  <> io.data_sram_addr
  core.io.dCache.sram_wdata <> io.data_sram_wdata

  core.io.debug.wb_pc       <> io.debug_wb_pc
  core.io.debug.wb_rf_wen   <> io.debug_wb_rf_wen
  core.io.debug.wb_rf_wnum  <> io.debug_wb_rf_wnum
  core.io.debug.wb_rf_wdata <> io.debug_wb_rf_wdata
}
