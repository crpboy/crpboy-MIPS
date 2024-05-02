package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._

class CP0 extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val write = new Bundle {
      val en   = Input(Bool())
      val data = Input(UInt(DATA_WIDTH.W))
      val addr = Input(UInt(REG_WIDTH.W))
      val sel  = Input(UInt(3.W))
    }
    val read = new Bundle {
      val addr = Input(UInt(REG_WIDTH.W))
      val sel  = Input(UInt(3.W))
      val data = Output(UInt(DATA_WIDTH.W))
    }
  })

  val badvaddr = new Cp0BadVAddr
  val count    = new Cp0Count
  val compare  = new Cp0Compare
  val status   = new Cp0Status
  val cause    = new Cp0Cause
  val epc      = new Cp0EPC
  val seq = Seq(
    badvaddr,
    count,
    compare,
    status,
    cause,
    epc,
  )

  // write cp0 reg
  when(io.write.en) {
    seq.foreach(it => {
      when(it.getId.U === Cat(io.write.addr, io.write.sel)) {
        it.write(io.write.data)
      }
    })
  }
  // read cp0 reg
  io.read.data := MuxLookup(
    Cat(io.read.addr, io.read.sel),
    0.U,
    seq.map(it => it.getId.U -> it.data.asUInt),
  )

  // count
  val tick = RegInit(false.B)
  tick := !tick
  when(tick) { count.data := count.data + 1.U }

  // status

  val timestop = count.data === compare.data
}
