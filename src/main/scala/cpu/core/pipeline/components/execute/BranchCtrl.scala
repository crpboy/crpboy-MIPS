package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class BranchCtrl extends Module {
  val io = IO(new Bundle {
    val ctrl    = Input(new CtrlInfo)
    val inst    = Input(new InstInfoExt)
    val pc      = Input(UInt(PC_WIDTH.W))
    val pcNext  = Input(UInt(PC_WIDTH.W))
    val op1     = Input(UInt(DATA_WIDTH.W))
    val op2     = Input(UInt(DATA_WIDTH.W))
    val predict = Input(new BraInfo)
    val bres    = Output(new BraResult)
    val binfo   = Output(new BraInfo)
  })

  val op1 = io.op1.asSInt
  val op2 = io.op2.asSInt

  val eqRes  = op1 === op2
  val neRes  = op1 =/= op2
  val gtzRes = op1 > 0.S
  val ltzRes = op1 < 0.S
  val eqzRes = op1 === 0.S
  val gezRes = gtzRes || eqzRes
  val lezRes = ltzRes || eqzRes

  io.bres.isb   := !io.ctrl.ex && io.inst.fu === fu_bra
  io.bres.index := io.pc(BPU_INDEX_WIDTH + 1, 2)
  io.bres.bwen := io.bres.isb &&
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
  io.binfo.bwen   := io.bres.bwen =/= io.predict.bwen // this bwen means predict failed
  io.binfo.bwaddr := Mux(io.bres.bwen, io.predict.bwaddr, io.pcNext)

  if (isStatistic) {
    val debug_total   = RegInit(0.U(32.W))
    val debug_success = RegInit(0.U(32.W))
    dontTouch(debug_total)
    dontTouch(debug_success)
    val cango = !io.ctrl.iStall && !io.ctrl.stall
    when(cango && io.bres.isb) {
      debug_total := debug_total + 1.U
    }
    when(cango && io.bres.isb && !io.binfo.bwen) {
      debug_success := debug_success + 1.U
    }
  }
}
