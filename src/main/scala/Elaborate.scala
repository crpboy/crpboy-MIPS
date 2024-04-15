import firrtl.options.TargetDirAnnotation
import cpu._
import cpu.core._

object elaborateMain extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
    new CoreTop(), Array("--target-dir", "generated"),
  )
}