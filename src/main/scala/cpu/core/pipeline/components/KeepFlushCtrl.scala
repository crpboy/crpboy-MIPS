package cpu.core.pipeline.components

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

trait KeepFlushCtrlConst {
  val if_home  = 4.U
  val id_home  = 3.U
  val exe_home = 2.U
  val mem_home = 1.U
  val wb_home  = 0.U
}

/*
  TODO: 可能导致bug的控制问题
  如果两个不同阶段同时对一个部件发出keep请求
  会导致keep请求只被执行一次，导致时序问题
 */

class KeepFlushCtrl extends Module with KeepFlushCtrlConst {
  val io = IO(new Bundle {
    val ifreq  = Input(new CtrlRequest)
    val idreq  = Input(new CtrlRequest)
    val exereq = Input(new CtrlRequest)
    val memreq = Input(new CtrlRequest)
    val wbreq  = Input(new CtrlRequest)
    val keep   = Output(UInt(KFC_WIDTH.W))
    val flush  = Output(UInt(KFC_WIDTH.W))
  })
  io.keep  := io.ifreq.keep  | io.idreq.keep  | io.exereq.keep  | io.memreq.keep  | io.wbreq.keep
  io.flush := io.ifreq.flush | io.idreq.flush | io.exereq.flush | io.memreq.flush | io.wbreq.flush
}
