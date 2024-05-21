package cpu.common.bundles

import chisel3._
import chisel3.util._
import cpu.common.const.Const._

trait Cp0Base {
  val id: Int
  val sel: Int = 0
  val data: Data
  def getId = (id * 8 + sel).asUInt
  def write(value: UInt): Unit = {
    data := value.asTypeOf(data)
  }
}

class Cp0IndexBundle extends Bundle {
  val p     = Bool()
  val blank = UInt((31 - TLB_INDEX_WIDTH).W)
  val index = UInt(TLB_INDEX_WIDTH.W)
}
class Cp0Index extends Cp0Base {
  override val id = 0
  override val data = RegInit(
    0.U.asTypeOf(new Cp0IndexBundle),
  ).suggestName("rIndex")
}

class Cp0EntryLo0Bundle extends Bundle {
  val blank = UInt(6.W)
  val pfn0  = UInt(TLB_PFN_WIDTH.W)
  val c0    = UInt(TLB_C_WIDTH.W)
  val d0    = Bool()
  val v0    = Bool()
  val g0    = Bool()
}
class Cp0EntryLo0 extends Cp0Base {
  override val id = 2
  override val data = RegInit(
    0.U.asTypeOf(new Cp0EntryLo0Bundle),
  ).suggestName("rEntryLo0")
}

class Cp0EntryLo1Bundle extends Bundle {
  val blank = UInt(6.W)
  val pfn1  = UInt(TLB_PFN_WIDTH.W)
  val c1    = UInt(TLB_C_WIDTH.W)
  val d1    = Bool()
  val v1    = Bool()
  val g1    = Bool()
}
class Cp0EntryLo1 extends Cp0Base {
  override val id = 3
  override val data = RegInit(
    0.U.asTypeOf(new Cp0EntryLo1Bundle),
  ).suggestName("rEntryLo1")
}

class Cp0BadVAddr extends Cp0Base {
  override val id = 8
  override val data = RegInit(
    0.U(DATA_WIDTH.W),
  ).suggestName("rBadVAddr")
}

class Cp0Count extends Cp0Base {
  override val id = 9
  override val data = RegInit(
    0.U(DATA_WIDTH.W),
  ).suggestName("rCount")
}

class Cp0EntryHiBundle extends Bundle {
  val vpn2  = UInt(TLB_VPN2_WIDTH.W)
  val blank = UInt(5.W)
  val asid  = UInt(TLB_ASID_WIDTH.W)
}
class Cp0EntryHi extends Cp0Base {
  override val id = 10
  override val data = RegInit(
    0.U.asTypeOf(new Cp0EntryHiBundle),
  ).suggestName("rEntryHi")
}

class Cp0Compare extends Cp0Base {
  override val id = 11
  override val data = RegInit(
    0.U(DATA_WIDTH.W),
  ).suggestName("rCompare")
}

class Cp0StatusBundle extends Bundle {
  val x1  = UInt(9.W)      // blank
  val Bev = Bool()         // Bev: always 1
  val x2  = UInt(6.W)      // blank
  val IM  = Vec(8, Bool()) // IM: 中断屏蔽位，每一位分别控制外部/内部/软件中断的使能
  val x3  = UInt(6.W)      // blank
  val EXL = Bool()         // EXL: 例外级，发生例外时置1
  val IE  = Bool()         // IE: 全局中断使能位
}
class Cp0Status extends Cp0Base {
  override val id = 12
  override val data = RegInit({
    val init = WireDefault(0.U.asTypeOf(new Cp0StatusBundle))
    init.Bev := true.B
    init
  }).suggestName("rStatus")
}

class Cp0CauseBundle extends Bundle {
  val BD      = UInt(1.W)      // BD: 最近一次例外指令是否处于延迟槽内
  val TI      = UInt(1.W)      // TI: 计时器中断指示 1: 有待处理的计时器中断
  val x1      = UInt(14.W)     // blank
  val IP      = Vec(8, Bool()) // IP: 待处理的中断标识 7..2为硬件中断标识，1..0为软件中断标识，可被读写
  val x2      = UInt(1.W)      // blank
  val ExcCode = UInt(5.W)      // ExcCode: excode
  val x3      = UInt(2.W)      // blank
}

class Cp0Cause extends Cp0Base {
  override val id = 13
  override val data = RegInit(
    0.U.asTypeOf(new Cp0CauseBundle),
  ).suggestName("rCause")
}

class Cp0EPC extends Cp0Base {
  override val id = 14
  override val data = RegInit(
    0.U(DATA_WIDTH.W),
  ).suggestName("rEPC")
}
