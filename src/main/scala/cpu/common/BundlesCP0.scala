package cpu.common

import chisel3._
import chisel3.util._
import cpu.common.Const._

class Cp0Status extends Bundle {
  val x1  = UInt(9.W)
  val Bev = Bool()
  val x2  = UInt(6.W)
  val IM  = UInt(8.W) // IM : 中断屏蔽位，每一位分别控制外部/内部/软件中断的使能
  val x3  = UInt(6.W)
  val EXL = Bool()    // 例外级，发生例外时置1
  val IE  = Bool()    // 全局中断使能位
}

class Cp0Cause extends Bundle {
  val BD      = UInt(1.W) // 最近一次例外指令是否处于延迟槽内
  val TI      = UInt(1.W) // 计时器中断指示 1: 有待处理的计时器中断
  val x1      = UInt(14.W)
  val IP      = UInt(8.W) // 待处理的中断标识 7..2为硬件中断标识，1..0为软件中断标识，可被读写
  val x2      = UInt(1.W)
  val ExcCode = UInt(5.W)
  val x3      = UInt(2.W)
}
