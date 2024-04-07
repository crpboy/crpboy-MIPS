package cpu

import chisel3._
import chisel3.util._
import cpu._

class TopIO extends Bundle {}

class Top extends Module {
  val core = new CoreTop()
}
