package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class JumpCtrl extends Module {
  val io = IO(new Bundle {
    val ctrl    = Input(new CtrlInfo)
    val inst    = Input(new InstInfoExt)
    val regData = Input(UInt(DATA_WIDTH.W))
    val pc      = Input(UInt(PC_WIDTH.W))
    val isex    = Output(Bool())
    val out     = Output(new JmpInfo)
  })
  val valid  = io.inst.fu === fu_jmp
  val target = Cat(io.pc.asUInt(31, 28), (io.inst.imm << 2)(27, 0))
  val isrReg = io.inst.fuop === _jmp_rreg
  val iswReg = io.inst.fuop === _jmp_wreg
  io.isex     := valid && io.out.jwaddr(1, 0) =/= "b00".U
  io.out.jwen := valid && !io.ctrl.ex && !io.isex
  io.out.jwaddr := MuxLookup(
    isrReg,
    0.U,
    Seq(
      0.U -> target,
      1.U -> io.regData,
    ),
  )
}
