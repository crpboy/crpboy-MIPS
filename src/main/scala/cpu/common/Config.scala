package cpu.common

import chisel3._
import chisel3.util._

trait Config {
  // 是否处于上板实验状态
  // true -> 调用xilinx ip
  // false -> 添加debug电路，取消调用xilinx ip
  val isBuild = false

  // 乘除法器需要的时钟周期
  // 需要在xilinx ip当中同步修改
  val mulClockNum = 2
  val divClockNum = 8
}
