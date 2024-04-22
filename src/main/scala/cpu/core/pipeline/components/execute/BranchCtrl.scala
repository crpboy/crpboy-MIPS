package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class BranchCtrl extends Module {
  val io = IO(new Bundle {
    val inst      = Input(new InstInfoExt)
    val pc        = Input(UInt(PC_WIDTH.W))
    val rs        = Input(UInt(DATA_WIDTH.W))
    val rt        = Input(UInt(DATA_WIDTH.W))
    val binfo     = Output(new BraInfo)
  })
  val valid = io.inst.fu === fu_bra
  io.binfo.en := valid
  io.binfo.bwen := valid && MuxLookup(
    io.inst.fuop,
    false.B,
    Seq(
      bra_beq    -> (io.rs === io.rt),
      bra_bne    -> (io.rs =/= io.rt),
      bra_bgez   -> (io.rs >= 0.U),
      bra_bgtz   -> (io.rs > 0.U),
      bra_blez   -> (io.rs <= 0.U),
      bra_bltz   -> (io.rs < 0.U),
      bra_bltzal -> (io.rs >= 0.U),
      bra_bgezal -> (io.rs < 0.U),
    ),
  )
  io.binfo.bwaddr := io.pc + (io.inst.imm << 2)
}
