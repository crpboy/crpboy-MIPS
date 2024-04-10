import chisel3._
import chisel3.stage.ChiselStage
import firrtl.options.TargetDirAnnotation

import cpu.core.CoreTop

object elaborateMain extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
    new CoreTop(), Array("--target-dir", "generated"),
  )
  // println("-- MAIN RUNNING ---------")
}