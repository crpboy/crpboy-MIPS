package cpu.core.pipeline.components.cp0

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

// TODO: 需要添加例外优先级
// TODO: count的自增逻辑有误，在解决stall导致的mfc0错误后再改回
class CP0 extends Module {
  val io = IO(new Bundle {
    val wb    = Input(new ExInfo)       // <> wb
    val write = Input(new WriteCp0Info) // <> wb
    val read  = new ReadCp0Info         // <> exe
    val fetch = new Bundle {
      val isex   = Output(Bool())
      val eret   = Output(Bool())
      val eretpc = Output(UInt(PC_WIDTH.W))
    } // <> fetch
    val stall    = Input(Bool()) // temp
    val extIntIn = Input(UInt(6.W))
    val exInfo   = Output(new ExInfo)
  })

  // reg init
  val index    = new Cp0Index
  val enrtyLo0 = new Cp0EntryLo0
  val enrtyLo1 = new Cp0EntryLo1
  val badvaddr = new Cp0BadVAddr
  val count    = new Cp0Count
  val entryHi  = new Cp0EntryHi
  val compare  = new Cp0Compare
  val status   = new Cp0Status
  val cause    = new Cp0Cause
  val epc      = new Cp0EPC
  val seq = Seq(
    index,
    enrtyLo0,
    enrtyLo1,
    badvaddr,
    count,
    entryHi,
    compare,
    status,
    cause,
    epc,
  )

  // write
  val writepos = Cat(io.write.addr, io.write.sel)
  when(io.write.en) {
    seq.foreach(it => {
      when(it.getId === writepos) {
        it.write(io.write.data)
      }
    })
  }

  // count
  val tick = RegInit(false.B)
  tick := !tick
  when( /*tick &&*/ writepos =/= count.getId && !io.stall) { count.data := count.data + 1.U }

  // cause
  val timeOut = RegInit(false.B)
  when(count.data === compare.data && compare.data =/= 0.U) {
    when(io.stall) {
      timeOut := true.B
    }.otherwise {
      cause.data.TI := true.B
    }
  }
  when(writepos === compare.getId && io.write.en) {
    cause.data.TI := false.B
  }
  when(timeOut && !io.stall) {
    cause.data.TI := true.B
    timeOut       := false.B
  }
  cause.data.IP(7) := cause.data.TI

  // check exception
  val except = WireDefault(io.wb)
  // bad pc
  when(io.wb.eret && epc.data(1, 0) =/= "b00".U) {
    except.en       := true.B
    except.eret     := false.B
    except.excode   := ex_AdEL
    except.badvaddr := epc.data
    except.pc       := epc.data
  }
  // interrupt
  val hasInt =
    ((cause.data.IP.asUInt(7, 0) & status.data.IM.asUInt(7, 0)) =/= 0.U) &&
      status.data.IE.asBool && !status.data.EXL.asBool
  when(hasInt) {
    except.en     := true.B
    except.excode := ex_Int
  }

  io.exInfo := except

  // exception writeback
  when(except.en && !io.stall) {
    cause.data.ExcCode := except.excode
    badvaddr.data      := except.badvaddr
    when(!status.data.EXL) {
      status.data.EXL := true.B
      cause.data.BD   := except.slot
      epc.data        := Mux(except.slot, except.pc - 4.U, except.pc)
    }
  }.elsewhen(except.eret && !io.stall) {
    status.data.EXL := false.B
  }

  // <> fetch
  io.fetch.isex   := except.en
  io.fetch.eret   := except.eret
  io.fetch.eretpc := epc.data

  // read info
  val readpos = Cat(io.read.addr, io.read.sel)
  io.read.data := MuxLookup(readpos, 0.U)(
    seq.map(it => it.getId -> it.data.asUInt),
  )
}

/*

CP0 各个数据来源如下
read cp0  : exe
write cp0 : writeback
exception : writeback
eret      : writeback

一旦出现exception，就会使得前面所有指令被直接冲刷为0
同时当i部件出现exception的时候 使得下一时钟周期 [1, i] 全部寄存器写使能无效
目前仅有hilo寄存器的写使能发生在非wb阶段

疑问：精确例外是否相当于取消后续指令对寄存器的写使能？

 */
