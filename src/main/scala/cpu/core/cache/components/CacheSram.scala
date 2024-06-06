package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

// single port sram
// used in ICache(tagvSram, dataSram)
// xilinx ip
class xilinx_single_port_ram_read_first(
    RAM_WIDTH: Int,
    RAM_DEPTH: Int,
) extends BlackBox(
      Map(
        "RAM_WIDTH" -> RAM_WIDTH,
        "RAM_DEPTH" -> RAM_DEPTH,
      ),
    )
    with HasBlackBoxResource
    with HasBlackBoxInline {
  val io = IO(new Bundle {
    val addra = Input(UInt(log2Ceil(RAM_DEPTH).W)) // addr input
    val dina  = Input(UInt(RAM_WIDTH.W))           // data input
    val clka  = Input(Clock())                     // clock
    val wea   = Input(Bool())                      // write enable
    val douta = Output(UInt(RAM_WIDTH.W))          // data output
  })
  addResource("/xilinx_single_port_ram_read_first.sv")
}

// read & write port sram without strb
// used in DCache(tagvSram, dirtSram)
// chisel util
class ReadWritePortSram(
    LINE_WIDTH: Int = CACHE_LINE_WIDTH,
    LINE_DEPTH: Int = CACHE_LINE_DEPTH,
) extends Module {
  val io = IO(new Bundle {
    val raddr = Input(UInt(log2Ceil(LINE_DEPTH).W))
    val rdata = Output(UInt(LINE_WIDTH.W))
    val waddr = Input(UInt(log2Ceil(LINE_DEPTH).W))
    val wdata = Input(UInt(LINE_WIDTH.W))
    val wen   = Input(Bool())
  })
  val sram = SyncReadMem(LINE_DEPTH, UInt(LINE_WIDTH.W))
  when(io.wen) {
    sram.write(io.waddr, io.wdata)
  }
  io.rdata := sram(io.raddr).asTypeOf(io.rdata)

  def hazard = io.waddr === io.raddr && io.wen
}

// read & write port sram with strb
// used in DCache(dataSram)
// chisel util
class ReadWritePortSramMasked(
    LINE_WIDTH: Int = CACHE_LINE_WIDTH,
    LINE_DEPTH: Int = CACHE_LINE_DEPTH,
) extends Module {
  val io = IO(new Bundle {
    val raddr = Input(UInt())
    val rdata = Output(UInt(LINE_WIDTH.W))
    val waddr = Input(UInt(log2Ceil(LINE_DEPTH).W))
    val wdata = Input(UInt(LINE_WIDTH.W))
    val wstrb = Input(UInt((LINE_WIDTH / BYTE_WIDTH).W))
    val wen   = Input(Bool())
  })
  def typex = Vec(LINE_WIDTH / BYTE_WIDTH, UInt(LINE_WIDTH.W))
  val sram  = SyncReadMem(LINE_DEPTH, typex)
  when(io.wen) {
    sram.write(io.waddr, io.wdata.asTypeOf(typex), io.wstrb.asUInt.asBools)
  }
  io.rdata := sram(io.raddr).asTypeOf(io.rdata)

  def hazard = io.waddr === io.raddr && io.wen
}

// not used, multi-port sram
class MultiPortSram(
    dataNum: Int,
    dataWidth: Int,
    numReadPorts: Int,
    numWritePorts: Int,
    numReadwritePorts: Int,
) extends Module {
  val io = IO(new SRAMInterface(dataNum, UInt(dataWidth.W), numReadPorts, numWritePorts, numReadwritePorts))
  io :<>= SRAM(dataNum, UInt(dataWidth.W), numReadPorts, numWritePorts, numReadwritePorts)
}
