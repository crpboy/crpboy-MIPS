package cpu.core.pipeline.components.cp0

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._

// TODO 完善CP0数据通路
class CP0 extends Module {
  val io = IO(new Bundle {
    val except = Input(new ExInfoWB)
    val write  = new WriteCp0Info
    val read   = new ReadCp0Info
  })

  // reg init
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
}
