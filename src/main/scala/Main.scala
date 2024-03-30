import chisel3._
import chisel3.stage.ChiselStage
import firrtl.options.TargetDirAnnotation

// import cpu._

object Main extends App {
	(new chisel3.stage.ChiselStage).emitVerilog(
		new CrpboyMips(), Array("--target-dir", "generated"),
	)
}
