package cpu

import chisel3._
import chisel3.util._

import cpu.core.pipeline._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.core.cache.top._

class TopIO extends Bundle {
  val ext_int = Input(UInt(INT_WIDTH.W))

  // axi interface
  val arid    = Output(UInt(AXI_ID_WIDTH.W))
  val araddr  = Output(UInt(AXI_ADDR_WIDTH.W))
  val arlen   = Output(UInt(AXI_LEN_WIDTH.W))
  val arsize  = Output(UInt(AXI_SIZE_WIDTH.W))
  val arburst = Output(UInt(AXI_BURST_WIDTH.W))
  val arlock  = Output(UInt(AXI_LOCK_WIDTH.W))
  val arcache = Output(UInt(AXI_CACHE_WIDTH.W))
  val arprot  = Output(UInt(AXI_PROT_WIDTH.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())

  val rid    = Input(UInt(AXI_ID_WIDTH.W))
  val rdata  = Input(UInt(AXI_DATA_WIDTH.W))
  val rresp  = Input(UInt(AXI_RESP_WIDTH.W))
  val rlast  = Input(Bool())
  val rvalid = Input(Bool())
  val rready = Output(Bool())

  val awid    = Output(UInt(AXI_ID_WIDTH.W))
  val awaddr  = Output(UInt(AXI_ADDR_WIDTH.W))
  val awlen   = Output(UInt(AXI_LEN_WIDTH.W))
  val awsize  = Output(UInt(AXI_SIZE_WIDTH.W))
  val awburst = Output(UInt(AXI_BURST_WIDTH.W))
  val awlock  = Output(UInt(AXI_LOCK_WIDTH.W))
  val awcache = Output(UInt(AXI_CACHE_WIDTH.W))
  val awprot  = Output(UInt(AXI_PROT_WIDTH.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())

  val wid    = Output(UInt(AXI_ID_WIDTH.W))
  val wdata  = Output(UInt(AXI_DATA_WIDTH.W))
  val wstrb  = Output(UInt(AXI_STRB_WIDTH.W))
  val wlast  = Output(Bool())
  val wvalid = Output(Bool())
  val wready = Input(Bool())

  val bid    = Input(UInt(AXI_ID_WIDTH.W))
  val bresp  = Input(UInt(AXI_RESP_WIDTH.W))
  val bvalid = Input(Bool())
  val bready = Output(Bool())

  // debug interface
  val debug_wb_pc       = Output(UInt(PC_WIDTH.W))
  val debug_wb_rf_wen   = Output(UInt(WEN_WIDTH.W))
  val debug_wb_rf_wnum  = Output(UInt(REG_WIDTH.W))
  val debug_wb_rf_wdata = Output(UInt(DATA_WIDTH.W))
}

class mycpu_top extends Module with Config {
  val io    = FlatIO(new TopIO)
  val core  = Module(new CoreTop)
  val cache = Module(new CacheTop)

  val rstTmp = !reset.asBool
  core.reset  <> rstTmp
  cache.reset <> rstTmp

  core.io.iCache <> cache.io.iCache
  core.io.dCache <> cache.io.dCache

  io.ext_int <> core.io.ext_int

  // axi - ar
  io.arid    <> cache.io.axi.ar.bits.id
  io.araddr  <> cache.io.axi.ar.bits.addr
  io.arlen   <> cache.io.axi.ar.bits.len
  io.arsize  <> cache.io.axi.ar.bits.size
  io.arburst <> cache.io.axi.ar.bits.burst
  io.arlock  <> cache.io.axi.ar.bits.lock
  io.arcache <> cache.io.axi.ar.bits.cache
  io.arprot  <> cache.io.axi.ar.bits.prot
  io.arvalid <> cache.io.axi.ar.valid
  io.arready <> cache.io.axi.ar.ready

  // axi - r
  io.rid    <> cache.io.axi.r.bits.id
  io.rdata  <> cache.io.axi.r.bits.data
  io.rresp  <> cache.io.axi.r.bits.resp
  io.rlast  <> cache.io.axi.r.bits.last
  io.rvalid <> cache.io.axi.r.valid
  io.rready <> cache.io.axi.r.ready

  // axi - aw
  io.awid    <> cache.io.axi.aw.bits.id
  io.awaddr  <> cache.io.axi.aw.bits.addr
  io.awlen   <> cache.io.axi.aw.bits.len
  io.awsize  <> cache.io.axi.aw.bits.size
  io.awburst <> cache.io.axi.aw.bits.burst
  io.awlock  <> cache.io.axi.aw.bits.lock
  io.awcache <> cache.io.axi.aw.bits.cache
  io.awprot  <> cache.io.axi.aw.bits.prot
  io.awvalid <> cache.io.axi.aw.valid
  io.awready <> cache.io.axi.aw.ready

  // axi - w
  io.wid    <> cache.io.axi.w.bits.id
  io.wdata  <> cache.io.axi.w.bits.data
  io.wstrb  <> cache.io.axi.w.bits.strb
  io.wlast  <> cache.io.axi.w.bits.last
  io.wvalid <> cache.io.axi.w.valid
  io.wready <> cache.io.axi.w.ready

  // axi - b
  io.bid    <> cache.io.axi.b.bits.id
  io.bresp  <> cache.io.axi.b.bits.resp
  io.bvalid <> cache.io.axi.b.valid
  io.bready <> cache.io.axi.b.ready

  // debug info
  io.debug_wb_pc       <> core.io.debug.wb_pc
  io.debug_wb_rf_wen   <> core.io.debug.wb_rf_wen
  io.debug_wb_rf_wnum  <> core.io.debug.wb_rf_wnum
  io.debug_wb_rf_wdata <> core.io.debug.wb_rf_wdata
}
/*
mycpu_top u_cpu(
    .int       (6'd0          ),   //high active

    .aclk      (cpu_clk       ),
    .aresetn   (cpu_resetn    ),   //low active

    .arid      (cpu_arid      ),
    .araddr    (cpu_araddr    ),
    .arlen     (cpu_arlen     ),
    .arsize    (cpu_arsize    ),
    .arburst   (cpu_arburst   ),
    .arlock    (cpu_arlock    ),
    .arcache   (cpu_arcache   ),
    .arprot    (cpu_arprot    ),
    .arvalid   (cpu_arvalid   ),
    .arready   (cpu_arready   ),

    .rid       (cpu_rid       ),
    .rdata     (cpu_rdata     ),
    .rresp     (cpu_rresp     ),
    .rlast     (cpu_rlast     ),
    .rvalid    (cpu_rvalid    ),
    .rready    (cpu_rready    ),

    .awid      (cpu_awid      ),
    .awaddr    (cpu_awaddr    ),
    .awlen     (cpu_awlen     ),
    .awsize    (cpu_awsize    ),
    .awburst   (cpu_awburst   ),
    .awlock    (cpu_awlock    ),
    .awcache   (cpu_awcache   ),
    .awprot    (cpu_awprot    ),
    .awvalid   (cpu_awvalid   ),
    .awready   (cpu_awready   ),

    .wid       (cpu_wid       ),
    .wdata     (cpu_wdata     ),
    .wstrb     (cpu_wstrb     ),
    .wlast     (cpu_wlast     ),
    .wvalid    (cpu_wvalid    ),
    .wready    (cpu_wready    ),

    .bid       (cpu_bid       ),
    .bresp     (cpu_bresp     ),
    .bvalid    (cpu_bvalid    ),
    .bready    (cpu_bready    ),

    //debug interface
    .debug_wb_pc      (debug_wb_pc      ),
    .debug_wb_rf_wen  (debug_wb_rf_wen  ),
    .debug_wb_rf_wnum (debug_wb_rf_wnum ),
    .debug_wb_rf_wdata(debug_wb_rf_wdata)
);
 */
