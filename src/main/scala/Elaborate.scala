import firrtl.options.TargetDirAnnotation
import cpu._

object elaborateMain extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
    new mycpu_top(), Array("--target-dir", "generated"),
  )
}