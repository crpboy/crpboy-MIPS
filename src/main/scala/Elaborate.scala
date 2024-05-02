import firrtl.options.TargetDirAnnotation
import chisel3._
import chisel3.util._
import cpu._
import cpu.core._

object elaborateMain extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
    new mycpu_top(), Array("--target-dir", "generated"),
    // new TestTop(), Array("--target-dir", "generated"),
  )
}