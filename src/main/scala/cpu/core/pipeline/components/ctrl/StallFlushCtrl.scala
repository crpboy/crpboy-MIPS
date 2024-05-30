package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class StallFlushCtrl extends Module {
  val io = IO(new Bundle {
    val ifreq  = Input(new CtrlRequest)
    val idreq  = Input(new CtrlRequest)
    val exereq = Input(new CtrlRequestExecute)
    val memreq = Input(new CtrlRequest)
    val wbreq  = Input(new CtrlRequest)
    val stall  = Output(UInt(CTRL_WIDTH.W))
    val bubble = Output(UInt(CTRL_WIDTH.W))
    val flush  = Output(UInt(CTRL_WIDTH.W))
  })
  val block: UInt = Cat(
    io.ifreq.block,
    io.idreq.block,
    io.exereq.block,
    io.memreq.block,
    io.wbreq.block,
  )
  val clear: UInt = Cat(
    io.ifreq.clear,
    io.idreq.clear,
    io.exereq.clear,
    io.memreq.clear,
    io.wbreq.clear,
  )
  val branch = io.exereq.branchPause

  val ifstall  = block(4) | block(3) | block(2) | block(1) | block(0)
  val ifbubble = false.B
  val ifflush  = clear(4) | clear(3) | clear(2) | clear(1) | clear(0)

  val idstall  = block(3) | block(2) | block(1) | block(0)
  val idbubble = block(4) | branch
  val idflush  = clear(3) | clear(2) | clear(1) | clear(0)

  val exestall  = block(2) | block(1) | block(0)
  val exebubble = block(3)
  val exeflush  = clear(2) | clear(1) | clear(0)

  val memstall  = block(1) | block(0)
  val membubble = block(2)
  val memflush  = clear(1) | clear(0)

  val wbstall  = block(0)
  val wbbubble = block(1)
  val wbflush  = clear(0)

  io.stall  := Cat(ifstall, idstall, exestall, memstall, wbstall)
  io.flush  := Cat(ifflush, idflush, exeflush, memflush, wbflush)
  io.bubble := Cat(ifbubble, idbubble, exebubble, membubble, wbbubble)
}

/*
  我们来分析一下流水线的运行状态
  无非是三种：
  A. 正常运行：每个Unit继承上一级的状态
  B. 在x-unit处阻塞：
    [1, x] -> stall; x+1 -> flush; [x+2, n] -> normal
    一般用于依赖于后续元件的使能信号，或者等待元件内部使能信号的情况
    绝对不能在缺使能信号的时候，使用阻塞状态stall自己，否则会产生死锁
  C. 需要清除x处指令：
    x+1 -> flush; otherwise -> normal
  从上述可以总结一些stall-flush的特点
    (1) stall元件的下一个为flush，后续为正常
        所以fetch处的flush信号无效
    (2) 令[x,y]的状态为stall, stall, ... flush
        则x-1处的inst被清除
        同时会在y处产生一个气泡
        特殊情况：只在y处进行flush，则会清除y-1的inst
  所以可以直接使用两个信号：block & clear
  来表示当前元件的inst是否需要特殊处理
  对于 id = i, signal = block[i], clear[i]
    stall [1 to i]  |= block[i]
    flush[i+1]      |= block[i]
    flush[1 to i+1] |= clear[i]
 */