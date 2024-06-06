package cpu.core.pipeline.components.memory

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class MemAccess extends Module {
  val io = IO(new Bundle {
    val preInfo = Input(new MemPreReqInfo)
    val memByte = Input(UInt(2.W))
    val dCache  = new DCacheIOMem

    val inst = Input(new InstInfo)
    val data = Input(UInt(DATA_WIDTH.W)) // write data
    val ctrl = Input(new CtrlInfo)

    val out = Output(UInt(DATA_WIDTH.W))
  })

  val dCache  = io.dCache
  val rdata   = io.dCache.respData
  val isload  = io.inst.fu === fu_mem && io.inst.wb
  val req     = Wire(new MemReqInfo)
  val memByte = io.memByte

  // send request
  req.valid := io.preInfo.valid
  req.wen   := io.preInfo.wen
  req.addr  := io.preInfo.addr
  req.size := MuxLookup(io.inst.fuop, 0.U)(
    Seq(
      mem_lw  -> 2.U,
      mem_lh  -> 1.U,
      mem_lhu -> 1.U,
      mem_lb  -> 0.U,
      mem_lbu -> 0.U,
      mem_lwl -> 2.U,
      mem_lwr -> 2.U,
      mem_sw  -> 2.U,
      mem_swl -> 2.U,
      mem_swr -> 2.U,
      mem_sh  -> 1.U,
      mem_sb  -> 0.U,
    ),
  )
  req.wstrb := MuxLookup(io.inst.fuop, 0.U)(
    Seq(
      mem_sb -> MuxLookup(memByte, 0.U)(
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b0100".U,
          "b01".U -> "b0010".U,
          "b00".U -> "b0001".U,
        ),
      ),
      mem_sh -> Mux(
        memByte(1).asBool,
        "b1100".U,
        "b0011".U,
      ),
      mem_sw -> "b1111".U,
      mem_swl -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> "b0001".U,
          "b01".U -> "b0011".U,
          "b10".U -> "b0111".U,
          "b11".U -> "b1111".U,
        ),
      ),
      mem_swr -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> "b1111".U,
          "b01".U -> "b1110".U,
          "b10".U -> "b1100".U,
          "b11".U -> "b1000".U,
        ),
      ),
    ),
  )
  req.wdata := MuxLookup(io.inst.fuop, io.data)(
    Seq(
      mem_sb -> Fill(4, io.data(7, 0)),
      mem_sh -> Fill(2, io.data(15, 0)),
      mem_swl -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> Cat(0.U(24.W), io.data(31, 24)),
          "b01".U -> Cat(0.U(16.W), io.data(31, 16)),
          "b10".U -> Cat(0.U(8.W), io.data(31, 8)),
          "b11".U -> io.data,
        ),
      ),
      mem_swr -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> io.data,
          "b01".U -> Cat(io.data(23, 0), 0.U(8.W)),
          "b10".U -> Cat(io.data(15, 0), 0.U(16.W)),
          "b11".U -> Cat(io.data(7, 0), 0.U(24.W)),
        ),
      ),
    ),
  )
  io.dCache.req := req

  // read info process
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
  io.out := data

  // handshake
  dCache.coreReady := !io.ctrl.stall && !io.ctrl.iStall
}
