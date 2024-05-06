import chisel3._
import chisel3.util._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import cpu.core.pipeline.CoreTop
import cpu.mycpu_top

class SimpleTest extends AnyFlatSpec with ChiselScalatestTester {
  "CoreTop_c0_read_write" should "pass" in {
    test(new CoreTop)
      .withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.io.dCache.sram_wdata.poke("b1".U)
        c.clock.step()
      }
  }
}
