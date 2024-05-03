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
    val exInfo  = Output(new ExInfoWB)

    val in    = Input(new StageMemoryWriteback)
    val out   = Output(new WBInfo)
    val debug = Output(new DebugIO)
  })
  val input = io.in

  io.out.wen   := input.inst.wb
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := input.data

  val except = Wire(new ExInfoWB)
  val cp0en  = input.inst.fu === fu_cp0
  except.en       := cp0en
  except.slot     := input.exInfo.slot
  except.badvaddr := input.exInfo.badvaddr
  except.excode   := input.exInfo.excode
  except.eret     := cp0en && input.inst.fuop === cp0_eret
  except.pc       := Mux(input.exInfo.slot, input.debug_pc, input.debug_pc - 4.U)

  io.ctrlreq.block := false.B
  io.ctrlreq.clear := false.B

  io.exInfo            := except
  io.debug.wb_pc       := input.debug_pc
  io.debug.wb_rf_wdata := input.data
  io.debug.wb_rf_wen   := Mux(input.inst.wb, WB_EN, WB_NO)
  io.debug.wb_rf_wnum  := input.inst.rd
}
