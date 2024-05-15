package cpu.common.const

import chisel3._
import chisel3.util._

trait Config {
  // 是否处于上板实验状态
  // true -> 调用xilinx ip
  // false -> 添加debug电路，取消调用xilinx ip
  val isBuild = false

  // 乘除法器需要的时钟周期
  // 上板时除法使用握手信号，不需要给定周期
  val mulClockNum = 2
  val divClockNum = 8
}
