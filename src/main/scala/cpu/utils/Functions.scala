package cpu.utils

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

object Functions {
  def signedExtend(data: UInt, width: Int = DATA_WIDTH): UInt = {
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
    PopCount(data)
  }
  def getclz(data: UInt): UInt = {
    data.getWidth.U - PopCount(data)
  }
  def bhtUpdate(data: UInt, bit: Bool): UInt = {
    Cat(data(data.getWidth - 2, 1), bit)
  }
  def cacheGetTag(data: UInt): UInt = {
    data(data.getWidth - 1, CACHE_INDEX_WIDTH + CACHE_OFFSET_WIDTH)
  }
  def cacheGetIndex(data: UInt): UInt = {
    data(CACHE_INDEX_WIDTH + CACHE_OFFSET_WIDTH - 1, CACHE_OFFSET_WIDTH)
  }
  def cacheGetOffset(data: UInt): UInt = {
    data(CACHE_OFFSET_WIDTH - 1, 0)
  }
  def mmuJudgeUnmapped(addr: UInt): Bool = {
    KSEG0_BEGIN <= addr && addr < KSEG2_BEGIN
  }
  def mmuJudgeUncached(addr: UInt): Bool = {
    KSEG1_BEGIN <= addr && addr < KSEG2_BEGIN
  }
  def getUncached(addr: UInt): Bool = {
    if (isNormalCache) {
      mmuJudgeUncached(addr)
    } else {
      isAllUncached.B
    }
  }
}
