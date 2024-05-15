package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class MemReq extends Module {
  val io = IO(new Bundle {
    val reqInfo  = Output(new MemReqInfo)
    val op1      = Input(UInt(DATA_WIDTH.W))
    val op2      = Input(UInt(DATA_WIDTH.W))
    val inst     = Input(new InstInfoExt)
    val ctrl     = Input(new CtrlInfo)
    val exLoad   = Output(Bool())
    val exStore  = Output(Bool())
    val badvaddr = Output(UInt(ADDR_WIDTH.W))
    val memByte  = Output(UInt(2.W))
  })

  val en      = io.inst.fu === fu_mem
  val vaddr   = io.op1 + io.inst.imm // TODO: 这里需要接入alu
  val memByte = vaddr(1, 0)
  io.memByte := memByte

  io.reqInfo.valid := en && !io.exLoad && !io.exStore && !io.ctrl.ex
  io.reqInfo.wen   := !io.inst.wb
  io.reqInfo.addr := Mux(
    io.inst.fuop === mem_lwl || io.inst.fuop === mem_swl,
    vaddr,
    Cat(vaddr(DATA_WIDTH - 1, 2), 0.U(2.W)),
  )
  io.reqInfo.size := MuxLookup(io.inst.fuop, 0.U)(
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
  io.reqInfo.wstrb := MuxLookup(io.inst.fuop, 0.U)(
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
  io.reqInfo.wdata := MuxLookup(io.inst.fuop, io.op2)(
    Seq(
      mem_sb -> Fill(4, io.op2(7, 0)),
      mem_sh -> Fill(2, io.op2(15, 0)),
      mem_swl -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> Cat(0.U(24.W), io.op2(31, 24)),
          "b01".U -> Cat(0.U(16.W), io.op2(31, 16)),
          "b10".U -> Cat(0.U(8.W), io.op2(31, 8)),
          "b11".U -> io.op2,
        ),
      ),
      mem_swr -> MuxLookup(memByte, 0.U)(
        Seq(
          "b00".U -> io.op2,
          "b01".U -> Cat(io.op2(23, 0), 0.U(8.W)),
          "b10".U -> Cat(io.op2(15, 0), 0.U(16.W)),
          "b11".U -> Cat(io.op2(7, 0), 0.U(24.W)),
        ),
      ),
    ),
  )
  io.exLoad := en && MuxLookup(io.inst.fuop, false.B)(
    Seq(
      mem_lw  -> (memByte(1, 0) =/= "b00".U),
      mem_lh  -> (memByte(0) =/= "b0".U),
      mem_lhu -> (memByte(0) =/= "b0".U),
    ),
  )
  io.exStore := en && MuxLookup(io.inst.fuop, false.B)(
    Seq(
      mem_sw -> (memByte(1, 0) =/= "b00".U),
      mem_sh -> (memByte(0) =/= "b0".U),
    ),
  )
  io.badvaddr := vaddr
}
