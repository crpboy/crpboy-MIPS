package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class LoadAccess extends Module {
  val io = IO(new Bundle {
    val dCache  = new DCacheIOMem
    val inst    = Input(new InstInfo)
    val data    = Input(UInt(DATA_WIDTH.W))
    val memByte = Input(UInt(2.W))
    val ctrl    = Input(new CtrlInfo)
    val block   = Output(Bool())
    val out     = Output(UInt(DATA_WIDTH.W))
  })
  val rdata  = io.dCache.data
  val isload = io.inst.fu === fu_mem && io.inst.wb
  val word = Mux(
    io.inst.fuop === _mem_lw,
    rdata,
    MuxLookup(io.inst.fuop, 0.U)(
      Seq(
        mem_lb -> MuxLookup(io.memByte, 0.U)(
          Seq(
            "b00".U -> signedExtend(rdata(7, 0)),
            "b01".U -> signedExtend(rdata(15, 8)),
            "b10".U -> signedExtend(rdata(23, 16)),
            "b11".U -> signedExtend(rdata(31, 24)),
          ),
        ),
        mem_lbu -> MuxLookup(io.memByte, 0.U)(
          Seq(
            "b00".U -> zeroExtend(rdata(7, 0)),
            "b01".U -> zeroExtend(rdata(15, 8)),
            "b10".U -> zeroExtend(rdata(23, 16)),
            "b11".U -> zeroExtend(rdata(31, 24)),
          ),
        ),
        mem_lh -> Mux(
          io.memByte(1).asBool,
          signedExtend(rdata(31, 16)),
          signedExtend(rdata(15, 0)),
        ),
        mem_lhu -> Mux(
          io.memByte(1).asBool,
          zeroExtend(rdata(31, 16)),
          zeroExtend(rdata(15, 0)),
        ),
      ),
    ),
  )
  val data = Mux(
    isload,
    MuxLookup(io.inst.fuop, word)(
      Seq(
        mem_lwl -> MuxLookup(io.memByte, 0.U)(
          Seq(
            "b00".U -> Cat(word(7, 0), io.data(23, 0)),
            "b01".U -> Cat(word(15, 0), io.data(15, 0)),
            "b10".U -> Cat(word(23, 0), io.data(7, 0)),
            "b11".U -> word,
          ),
        ),
        mem_lwr -> MuxLookup(io.memByte, 0.U)(
          Seq(
            "b00".U -> word,
            "b01".U -> Cat(io.data(31, 24), word(31, 8)),
            "b10".U -> Cat(io.data(31, 16), word(31, 16)),
            "b11".U -> Cat(io.data(31, 8), word(31, 24)),
          ),
        ),
      ),
    ),
    io.data,
  )
  io.out              := data
  io.block            := isload && io.dCache.stall
  io.dCache.coreReady := !io.ctrl.stall
}
