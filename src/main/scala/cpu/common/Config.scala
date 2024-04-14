package cpu.common

import chisel3._
import chisel3.util._

object Config extends ConfigList

trait ConfigList {
  val build = false
  
}