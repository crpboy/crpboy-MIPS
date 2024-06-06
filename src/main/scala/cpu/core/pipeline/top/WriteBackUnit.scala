package cpu.core.pipeline.top

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val dHazard = Output(new DataHazard)
    val ctrlreq = Output(new CtrlRequest)
    val ctrl    = Input(new CtrlInfo)
    val cp0 = new Bundle {
      val exout = Output(new ExInfo)
      val wCp0  = Flipped(new WriteCp0Info)
      val exres = Input(new ExInfo)
    }
    val exe = new Bundle {
      val slot = Input(Bool())
      val ex   = Input(Bool())
    }
    val mem = new Bundle {
      val slot = Input(Bool())
      val ex   = Input(Bool())
    }
    val in    = Flipped(Decoupled(new StageMemoryWriteback))
    val out   = Output(new WBInfo)
    val debug = Output(new DebugIO)
  })
  val input = io.in.bits

  io.dHazard.wen   := input.inst.wb
  io.dHazard.waddr := input.inst.rd
  io.dHazard.wdata := input.data

  val valid = io.in.valid

  // <> regfile
  io.out.wen   := !io.cp0.exres.en && input.inst.wb && valid
  io.out.wdata := input.data
  io.out.waddr := input.inst.rd

  // <> cp0 (except)
  val except = WireDefault(input.exInfo)
  except.slot := input.slot
  when(input.exInfo.pc === 0.U) {
    except.pc := input.debug_pc
  }
  when((io.exe.slot && io.exe.ex) || (io.mem.slot && io.mem.ex)) {
    except.en := false.B
  }
  when(!valid) {
    except.en   := false.B
    except.eret := false.B
  }

  // <> cp0 (mtc0)
  io.cp0.wCp0.en   := input.inst.fu === fu_sp && input.inst.fuop === cp0_mtc0 && valid && !io.ctrl.iStall
  io.cp0.wCp0.data := input.data
  io.cp0.wCp0.addr := input.inst.rd
  io.cp0.wCp0.sel  := input.exSel
  io.cp0.exout     := except

  io.ctrlreq.block := false.B
  io.ctrlreq.clear := io.cp0.exres.en || io.cp0.exres.eret
  io.in.ready      := true.B

  io.debug.wb_pc       := input.debug_pc
  io.debug.wb_rf_wdata := io.out.wdata
  io.debug.wb_rf_wen   := Mux(io.out.wen && !io.ctrl.iStall, WB_EN, WB_NO)
  io.debug.wb_rf_wnum  := io.out.waddr
}
