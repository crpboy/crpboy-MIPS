package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.Const._

class PreDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(INST_WIDTH.W))
    val isbr = Output(Bool())
  })
  val res: List[UInt] = ListLookup(
    io.inst,
    List(fuop_nop, false.B),
    Array(
      J      -> List(jmp_j, true.B),
      JAL    -> List(jmp_jal, true.B),
      JR     -> List(jmp_jr, true.B),
      JALR   -> List(jmp_jalr, true.B),
      BEQ    -> List(bra_beq, true.B),
      BNE    -> List(bra_bne, true.B),
      BGEZ   -> List(bra_bgez, true.B),
      BGTZ   -> List(bra_bgtz, true.B),
      BLEZ   -> List(bra_blez, true.B),
      BLTZ   -> List(bra_bltz, true.B),
      BLTZAL -> List(bra_bltzal, true.B),
      BGEZAL -> List(bra_bgezal, true.B),
    ),
  )
  val fuop :: isjInfo :: Nil = res
  // TODO: fuop might be added into pipeline signal

  io.isbr := isjInfo
}
