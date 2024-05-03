package cpu.core.pipeline.components.writeback

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val dHazard = Output(new DataHazard)
    val ctrlreq = Output(new CtrlRequest)
    val ctrl    = Input(new CtrlInfo)
    val cp0 = new Bundle {
      val exInfo = Output(new ExInfoToCp0)
      val wCp0   = Flipped(new WriteCp0Info)
    }
    val exfetch = new Bundle { val isex = Output(Bool()) }

    val in    = Input(new StageMemoryWriteback)
    val out   = Output(new WBInfo)
    val debug = Output(new DebugIO)
  })
  val input = io.in
  val cp0en = input.inst.fu === fu_cp0

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := input.data

  // <> regfile
  io.out.wen   := input.inst.wb
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  // <> cp0
  val iseret = cp0en && input.inst.fuop === cp0_eret
  val isex   = input.exInfo.en && !iseret
  val except = Wire(new ExInfoToCp0)

  except.en       := isex
  except.bd       := input.exInfo.slot
  except.badvaddr := input.exInfo.badvaddr
  except.excode   := input.exInfo.excode
  except.pc       := input.debug_pc

  io.cp0.wCp0.en   := !io.ctrl.ex && cp0en && input.inst.fuop === cp0_mtc0
  io.cp0.wCp0.data := input.data
  io.cp0.wCp0.addr := input.inst.rd
  io.cp0.wCp0.sel  := input.exSel
  io.cp0.exInfo    := except

  // <> fetch (except)
  io.exfetch.isex := isex

  io.ctrlreq.block := false.B
  io.ctrlreq.clear := false.B

  io.debug.wb_pc       := input.debug_pc
  io.debug.wb_rf_wdata := io.out.wdata
  io.debug.wb_rf_wen   := Mux(input.inst.wb, WB_EN, WB_NO)
  io.debug.wb_rf_wnum  := io.out.waddr
}
