import chisel3._
import chisel3.util._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import cpu.core.pipeline.CoreTop
import cpu.core.pipeline.components.decode.RegFile
import cpu.utils._

class SimpleTest extends AnyFlatSpec with ChiselScalatestTester {
  "regfile" should "pass" in {
    test(new RegFile)
      .withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.io.wb.wen.poke(true.B)
        c.io.wb.waddr.poke(1.U)
        c.io.wb.wdata.poke(3.U)
        c.clock.step()

        c.io.wb.waddr.poke(2.U)
        c.io.wb.wdata.poke(5.U)
        c.io.wb.wen.poke(true.B)
        c.clock.step()

        c.io.rsaddr.poke(1.U)
        c.io.rtaddr.poke(2.U)
        c.clock.step()

        c.io.rsdata.expect(3.U)
        c.io.rtdata.expect(5.U)
      }
  }
  "Core" should "pass" in {
    test(new CoreTop)
      .withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.io.iCache.sram_rdata.poke(0x20010014.U)
        c.clock.step() // addi r1 r0 0x0000001
        c.io.iCache.sram_rdata.poke(0x200200fa.U)
        c.clock.step() // addi r2 r0 0x0000002
        c.clock.step()
        c.clock.step()
        c.clock.step()
        c.clock.step()
        c.io.iCache.sram_rdata.poke(0x00221820.U)
        c.clock.step() // add r3 r1 r2
        c.clock.step()
        c.clock.step()
        c.clock.step()
        c.clock.step()
      }
  }
}
