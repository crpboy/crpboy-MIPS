package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class BranchCtrl extends Module {
  val io = IO(new Bundle {
    val ctrl  = Input(new CtrlInfo)
    val inst  = Input(new InstInfoExt)
    val pc    = Input(UInt(PC_WIDTH.W))
    val op1   = Input(UInt(DATA_WIDTH.W))
    val op2   = Input(UInt(DATA_WIDTH.W))
    val binfo = Output(new BraInfo)
  })

  val op1 = io.op1.asSInt
  val op2 = io.op2.asSInt

  val eqRes  = op1 === op2
  val neRes  = op1 =/= op2
  val gezRes = op1 >= 0.S
  val lezRes = op1 <= 0.S
  val gtzRes = op1 > 0.S
  val ltzRes = op1 < 0.S

  io.binfo.bwen := !io.ctrl.ex &&
    (io.inst.fu === fu_bra) &&
    MuxLookup(io.inst.fuop, false.B)(
      Seq(
        bra_beq    -> eqRes,
        bra_bne    -> neRes,
        bra_bgez   -> gezRes,
        bra_bgtz   -> gtzRes,
        bra_blez   -> lezRes,
        bra_bltz   -> ltzRes,
        bra_bgezal -> gezRes,
        bra_bltzal -> ltzRes,
      ),
    )
  io.binfo.bwaddr := io.pc + (io.inst.imm << 2)
}
