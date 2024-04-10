package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common.Config._
import cpu.common.Const._
import cpu.utils._

class GPRsIO extends Bundle {
  val en      = Input(Bool())
  val regAddr = Input(new RegAddrBundle)
  val wbInfo  = Input(new WBInfo)
  val regData = Output(new RegDataBundle)
}
class GPRs extends Module {
  val io   = IO(new GPRsIO)
  val reg  = Reg(Vec(REG_NUM, UInt(DATA_WIDTH.W)))
  io.regData.rs := reg(io.regAddr.rs)
  io.regData.rt := reg(io.regAddr.rt)
  when(io.wbInfo.wb) {
    reg(io.wbInfo.rd) := io.wbInfo.data
  }
}
