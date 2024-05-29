package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class xilinx_single_port_ram_read_first(RAM_WIDTH: Int, RAM_DEPTH: Int)
    extends BlackBox(Map("RAM_WIDTH" -> RAM_WIDTH, "RAM_DEPTH" -> RAM_DEPTH))
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
