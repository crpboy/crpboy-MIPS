package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

object Functions {
  def signExtend(data: UInt, width: Int = DATA_WIDTH): UInt = {
    val len = data.getWidth
    return Cat(Fill(width - len, data(len - 1)), data)
  }
  def zeroExtend(data: UInt, width: Int = DATA_WIDTH): UInt = {
    val len = data.getWidth
    return Cat(Fill(width - len, 0.U), data)
  }
  def zeroExtendHigh(data: UInt, width: Int = DATA_WIDTH): UInt = {
    val len = data.getWidth
    Cat(data, Fill(width - len, 0.U))
  }
  def getclo(data: UInt): UInt = {
    val clo = WireInit(32.U)
    for (i <- 0 until 32) {
      when(!data(i)) {
        clo := (31 - i).U
      }
    }
    return clo
  }
  def getclz(data: UInt): UInt = {
    val clz = WireInit(32.U)
    for (i <- 0 until 32) {
      when(data(i)) {
        clz := (31 - i).U
      }
    }
    return clz
  }
}
