package cpu.common

import chisel3._
import chisel3.util._
import cpu.common.Const._

class ExInfoBase extends Bundle {
  val isEx     = Bool()
  val isSlot   = Bool()
  val badvaddr = UInt(ADDR_WIDTH.W)
  val excode   = UInt(EX_LEN.W)
}
class ExInfoExt extends ExInfoBase {
  val eret = Bool()
  val pc   = UInt(PC_WIDTH.W)
}

trait Cp0Base {
  val id: Int
  val sel: Int = 0
  val data: Data
  def getId = id * 8 + sel
  def write(value: UInt) = {
    data := value.asTypeOf(data)
  }
}

class Cp0BadVAddr extends Cp0Base {
  override val id   = 8
  override val data = RegInit(0.U(DATA_WIDTH.W))
}

class Cp0Count extends Cp0Base {
  override val id   = 9
  override val data = RegInit(0.U(DATA_WIDTH.W))
}

class Cp0Compare extends Cp0Base {
  override val id   = 11
  override val data = RegInit(0.U(DATA_WIDTH.W))
}

class Cp0StatusBundle extends Bundle {
  val x1  = UInt(9.W) // blank
  val Bev = Bool()    // Bev: always 1
  val x2  = UInt(6.W) // blank
  val IM  = UInt(8.W) // IM: 中断屏蔽位，每一位分别控制外部/内部/软件中断的使能
  val x3  = UInt(6.W) // blank
  val EXL = Bool()    // EXL: 例外级，发生例外时置1
  val IE  = Bool()    // IE: 全局中断使能位
}
class Cp0Status extends Cp0Base {
  override val id = 12
  override val data = RegInit({
    val init = WireDefault(0.U.asTypeOf(new Cp0StatusBundle))
    init.Bev := true.B
    init
  })
}

class Cp0CauseBundle extends Bundle {
  val BD      = UInt(1.W)  // BD: 最近一次例外指令是否处于延迟槽内
  val TI      = UInt(1.W)  // TI: 计时器中断指示 1: 有待处理的计时器中断
  val x1      = UInt(14.W) // blank
  val IP      = UInt(8.W)  // IP: 待处理的中断标识 7..2为硬件中断标识，1..0为软件中断标识，可被读写
  val x2      = UInt(1.W)  // blank
  val ExcCode = UInt(5.W)  // ExcCode: excode
  val x3      = UInt(2.W)  // blank
}

class Cp0Cause extends Cp0Base {
  override val id   = 13
  override val data = RegInit(0.U.asTypeOf(new Cp0CauseBundle))
}

class Cp0EPC extends Cp0Base {
  override val id   = 14
  override val data = RegInit(0.U(DATA_WIDTH.W))
}
