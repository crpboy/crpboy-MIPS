package cpu.core.pipeline.components.ctrl

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

/*
  TODO: 可能导致bug的控制问题
  如果两个不同阶段同时对一个部件发出keep请求
  会导致keep请求只被执行一次，导致时序问题

  TODO: 创建使能信号对应的临时变量，然后全部或起来
  通过这种方式避免选择器嵌套，实现并行控制信号选择

  还有一种方案
  我们来分析一下流水线的运行状态
  无非是三种：
  A. 正常运行：每个Unit继承上一级的状态
  B. 在x-unit处阻塞：
    [1, x] -> keep; x+1 -> flush; [x+2, n] -> normal
    一般用于依赖于后续元件的使能信号，或者等待元件内部使能信号的情况
    绝对不能在缺使能信号的时候，使用阻塞状态keep自己，否则会产生死锁
  C. 需要清除x处指令：
    x+1 -> flush; otherwise -> normal
  从上述可以总结一些keep-flush的特点
    (1) keep元件的下一个为flush，后续为正常
        所以fetch处的flush信号无效
    (2) 令[x,y]的状态为keep, keep, ... flush
        则x-1处的inst被清除
        同时会在y处产生一个气泡
        特殊情况：只在y处进行flush，则会清除y-1的inst
  所以可以直接使用两个信号：block & clear
  来表示当前元件的inst是否需要特殊处理
  对于 id = i, signal = block[i], clear[i]
    keep[1 to i] |= block[i]
    flush[i+1]   |= clear[i]
 */

class KeepFlushCtrl extends Module {
  val io = IO(new Bundle {
    val ifreq  = Input(new CtrlRequest)
    val idreq  = Input(new CtrlRequest)
    val exereq = Input(new CtrlRequestExecute)
    val memreq = Input(new CtrlRequest)
    val wbreq  = Input(new CtrlRequest)
    val keep   = Output(UInt(5.W))
    val flush  = Output(UInt(5.W))
  })
  val block: UInt = Cat(
    io.ifreq.block,
    io.idreq.block,
    io.exereq.block,
    io.memreq.block,
    io.wbreq.block,
  )
  val clear: UInt = Cat(
    (io.ifreq.clear | io.exereq.branchPause),
    io.idreq.clear,
    io.exereq.clear,
    io.memreq.clear,
    io.wbreq.clear,
  )

  val ifkeep  = block(4) | block(3) | block(2) | block(1) | block(0)
  val ifflush = false.B

  val idkeep  = block(3) | block(2) | block(1) | block(0)
  val idflush = block(4) | clear(4)

  val exekeep  = block(2) | block(1) | block(0)
  val exeflush = block(3) | clear(3)

  val memkeep  = block(1) | block(0)
  val memflush = block(2) | clear(2)

  val wbkeep  = block(0)
  val wbflush = block(1) | clear(1)

  io.keep  := Cat(ifkeep, idkeep, exekeep, memkeep, wbkeep)
  io.flush := Cat(ifflush, idflush, exeflush, memflush, wbflush)
}
