package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

object Functions {
  def signedExtend(data: UInt, width: Int = DATA_WIDTH) = {
    val len = data.getWidth
    Cat(Fill(width - len, data(len - 1)), data)
  }
  def zeroExtend(data: UInt, width: Int = DATA_WIDTH) = {
    val len = data.getWidth
    Cat(Fill(width - len, 0.U), data)
  }
  def zeroExtendHigh(data: UInt, width: Int = DATA_WIDTH) = {
    val len = data.getWidth
    Cat(data, Fill(width - len, 0.U))
  }
}
