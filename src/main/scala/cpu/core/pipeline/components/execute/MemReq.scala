package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

class MemReq extends Module {
  val io = IO(new Bundle {
    val dCache = new Bundle {
      val sram_en    = Output(Bool())
      val sram_wen   = Output(UInt(WEN_WIDTH.W))
      val sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
    val rs       = Input(UInt(DATA_WIDTH.W))
    val rt       = Input(UInt(DATA_WIDTH.W))
    val inst     = Input(new InstInfoExt)
    val ctrl     = Input(new CtrlInfo)
    val exLoad   = Output(Bool())
    val exStore  = Output(Bool())
    val badvaddr = Output(UInt(ADDR_WIDTH.W))
    val memByte  = Output(UInt(2.W))
  })

  val en      = io.inst.fu === fu_mem
  val vaddr   = io.rs + io.inst.imm
  val memByte = vaddr(1, 0)

  io.memByte          := memByte
  io.dCache.sram_en   := en && !io.exLoad && !io.exStore && !io.ctrl.ex
  io.dCache.sram_addr := vaddr
  io.dCache.sram_wen := Mux(
    io.inst.wb || !io.dCache.sram_en,
    "b0000".U,
    MuxLookup(
      io.inst.fuop,
      0.U,
      Seq(
        mem_sb -> MuxLookup(
          memByte,
          0.U,
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
        mem_swl -> MuxLookup(
          memByte,
          0.U,
          Seq(
            "b00".U -> "b0001".U,
            "b01".U -> "b0011".U,
            "b10".U -> "b0111".U,
            "b11".U -> "b1111".U,
          ),
        ),
        mem_swr -> MuxLookup(
          memByte,
          0.U,
          Seq(
            "b00".U -> "b1111".U,
            "b01".U -> "b1110".U,
            "b10".U -> "b1100".U,
            "b11".U -> "b1000".U,
          ),
        ),
      ),
    ),
  )
  io.dCache.sram_wdata := MuxLookup(
    io.inst.fuop,
    io.rt,
    Seq(
      mem_sb -> Fill(4, io.rt(7, 0)),
      mem_sh -> Fill(2, io.rt(15, 0)),
      mem_swl -> MuxLookup(
        memByte,
        0.U,
        Seq(
          "b00".U -> Cat(0.U(24.W), io.rt(31, 24)),
          "b01".U -> Cat(0.U(16.W), io.rt(31, 16)),
          "b10".U -> Cat(0.U(8.W), io.rt(31, 8)),
          "b11".U -> io.rt,
        ),
      ),
      mem_swr -> MuxLookup(
        memByte,
        0.U,
        Seq(
          "b00".U -> io.rt,
          "b01".U -> Cat(io.rt(23, 0), 0.U(8.W)),
          "b10".U -> Cat(io.rt(15, 0), 0.U(16.W)),
          "b11".U -> Cat(io.rt(7, 0), 0.U(24.W)),
        ),
      ),
    ),
  )
  io.exLoad := en && MuxLookup(
    io.inst.fuop,
    false.B,
    Seq(
      mem_lw  -> (memByte(1, 0) =/= "b00".U),
      mem_lh  -> (memByte(0) =/= "b0".U),
      mem_lhu -> (memByte(0) =/= "b0".U),
    ),
  )
  io.exStore := en && MuxLookup(
    io.inst.fuop,
    false.B,
    Seq(
      mem_sw -> (memByte(1, 0) =/= "b00".U),
      mem_sh -> (memByte(0) =/= "b0".U),
    ),
  )
  io.badvaddr := vaddr
}
