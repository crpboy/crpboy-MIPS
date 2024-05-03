package cpu.core.pipeline.components.cp0

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._

// TODO 完善CP0数据通路
class CP0 extends Module {
  val io = IO(new Bundle {
    val wb    = Input(new ExInfoToCp0)  // <> wb
    val write = Input(new WriteCp0Info) // <> wb
    val eret = new Bundle {
      val en = Input(Bool())
      val pc = Output(UInt(PC_WIDTH.W))
    } // decode -> cp0 -> fetch
    val read = new ReadCp0Info // <> exe
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

  // count
  val tick = RegInit(false.B)
  tick := !tick
  when(tick) { count.data := count.data + 1.U }

  // TODO: BadVAddr signal isn't added
  val except = io.wb
  when(except.en) {
    cause.data.ExcCode := except.excode
    cause.data.BD      := except.bd
    status.data.EXL    := true.B
    epc.data           := Mux(except.bd, except.pc - 4.U, except.pc)
  }.elsewhen(io.eret.en) {
    status.data.EXL := false.B
  }

  // write cp0 reg
  val writepos = Cat(io.write.addr, io.write.sel)
  when(io.write.en) {
    seq.foreach(it => {
      when(it.getId.U === writepos) {
        it.write(io.write.data)
      }
    })
  }
  // read cp0 reg
  val readpos = Cat(io.read.addr, io.read.sel)
  io.read.data := MuxLookup(
    readpos,
    0.U,
    seq.map(it => it.getId.U -> it.data.asUInt),
  )
  io.eret.pc := epc.data
}

/*

CP0 各个数据来源如下
read cp0  : exe
write cp0 : writeback
exception : writeback
eret      : decoder

一旦出现exception，就会使得前面所有指令被直接冲刷为0
同时当i部件出现exception的时候 使得下一时钟周期 [1, i] 全部寄存器写使能无效
目前仅有hilo寄存器的写使能发生在非wb阶段

疑问：精确例外是否相当于取消后续指令对寄存器的写使能？

 */
