package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.utils._
import cpu.common.Const._

class JumpCtrl extends Module {
  val io = IO(new Bundle {
    val inst    = Input(new InstInfoExt)
    val regData = Input(new RegData)
    val out     = Output(new JmpInfo)
  })
  io.out.jwen := io.inst.fu === fu_jmp
  io.out.jwaddr := Mux(
    io.inst.fu === fu_jmp,
    MuxLookup(
      io.inst.fuop,
      0.U,
      Seq(
        jmp_j -> io.inst.imm,
      ),
    ),
    0.U,
  )
}
