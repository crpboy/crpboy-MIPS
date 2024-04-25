package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class BranchCtrl extends Module {
  val io = IO(new Bundle {
    val inst  = Input(new InstInfoExt)
    val pc    = Input(UInt(PC_WIDTH.W))
    val rs    = Input(UInt(DATA_WIDTH.W))
    val rt    = Input(UInt(DATA_WIDTH.W))
    val binfo = Output(new BraInfo)
  })
  val rs = io.rs.asSInt
  val rt = io.rt.asSInt
  io.binfo.bwen := (io.inst.fu === fu_bra) && MuxLookup(
    io.inst.fuop,
    false.B,
    Seq(
      bra_beq    -> (rs === rt),
      bra_bne    -> (rs =/= rt),
      bra_bgez   -> (rs >= 0.S),
      bra_bgtz   -> (rs > 0.S),
      bra_blez   -> (rs <= 0.S),
      bra_bltz   -> (rs < 0.S),
      bra_bgezal -> (rs >= 0.S),
      bra_bltzal -> (rs < 0.S),
    ),
  )
  io.binfo.bwaddr := io.pc + (io.inst.imm << 2)
}
