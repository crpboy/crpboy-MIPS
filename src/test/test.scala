package cpu

import chisel3._

object testMain extends App {
	(new chisel3.stage.ChiselStage).execute(
		Array("--target-dir", "generated"),
		Seq(ChiselGeneratorAnnotation()),
	)
}