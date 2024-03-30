import chisel3._
import chisel3.util._

// import cpu._

class CrpboyMips extends Module {
	val io = IO(new Bundle {
		val in = Input(UInt(4.W))
		val out = Output((UInt(4.W)))
	})
	io.out := io.in
}
