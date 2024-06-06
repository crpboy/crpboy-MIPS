package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class MemReq extends Module {
  val io = IO(new Bundle {
    val reqInfo  = Output(new MemPreReqInfo)

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
    Cat(vaddr(ADDR_WIDTH - 1, 2), 0.U(2.W)),
    vaddr,
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
