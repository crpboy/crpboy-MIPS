package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._

class JumpCtrl extends Module {
  val io = IO(new Bundle {
    val inst    = Input(new InstInfoExt)
    val out     = Output(new JmpInfo)
  })
  io.out.jwen := io.inst.fu === fu_jmp
  io.out.jwaddr := MuxLookup(
    io.inst.fuop,
    0.U,
    Seq(
      jmp_j -> io.inst.imm,
    ),
  )
}
