package cpu.core.pipeline.top

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.core.pipeline.components.decode._

// TODO: 中断例外的发生应该放到decode
class DecodeUnit extends Module {
  val io = IO(new Bundle {
    val wb      = Input(new WBInfo)
    val jinfo   = Output(new JmpInfo)
    val ctrlreq = Output(new CtrlRequest)
    val binfo   = Output(new BraInfo)
    val execute = new Bundle { val bres = Input(new BraResult) }
    val fetch   = new Bundle { val isJmp = Output(Bool()) }

    val exeDHazard = Input(new DataHazardExe)
    val memDHazard = Input(new DataHazardMem)
    val wbDHazard  = Input(new DataHazard)

    val ctrl = Input(new CtrlInfo)
    val in   = Flipped(Decoupled((new StageFetchDecode)))
    val out  = Decoupled(new StageDecodeExecute)
  })

  val decoder = Module(new Decoder).io
  val reg     = Module(new RegFile).io
  val jmp     = Module(new JumpCtrl).io
  val bpu     = Module(new BranchPredict).io

  val input  = io.in.bits
  val output = io.out.bits

  // data forward
  val rsdata = MuxCase(
    reg.rsdata,
    Seq(
      (io.exeDHazard.wen && io.exeDHazard.waddr === reg.rsaddr) -> io.exeDHazard.wdata,
      (io.memDHazard.wen && io.memDHazard.waddr === reg.rsaddr) -> io.memDHazard.wdata,
      (io.wbDHazard.wen && io.wbDHazard.waddr === reg.rsaddr)   -> io.wbDHazard.wdata,
    ),
  )
  val rtdata = MuxCase(
    reg.rtdata,
    Seq(
      (io.exeDHazard.wen && io.exeDHazard.waddr === reg.rtaddr) -> io.exeDHazard.wdata,
      (io.memDHazard.wen && io.memDHazard.waddr === reg.rtaddr) -> io.memDHazard.wdata,
      (io.wbDHazard.wen && io.wbDHazard.waddr === reg.rtaddr)   -> io.wbDHazard.wdata,
    ),
  )

  // decoder input
  input.inst <> decoder.rawInst
  input.pc   <> jmp.pc

  // reg
  decoder.rsaddr <> reg.rsaddr
  decoder.rtaddr <> reg.rtaddr
  io.wb          <> reg.wb

  // jump
  rsdata           <> jmp.regData
  decoder.instInfo <> jmp.inst
  io.ctrl          <> jmp.ctrl
  io.jinfo.jwen    := jmp.out.jwen
  io.jinfo.jwaddr  := jmp.out.jwaddr

  // branch
  bpu.inst := input.inst
  bpu.pc   := input.pc
  bpu.isb  := decoder.instInfo.fu === fu_bra
  bpu.bres := io.execute.bres
  bpu.ctrl := io.ctrl
  io.binfo := bpu.binfo

  // except
  val except   = WireDefault(input.exInfo)
  val instInfo = decoder.instInfo
  io.fetch.isJmp := instInfo.fu === fu_jmp || instInfo.fu === fu_bra
  when(instInfo.fu === fu_sp) {
    when(instInfo.fuop === cp0_syscall) {
      except.en     := true.B
      except.excode := ex_Sys
      except.pc     := input.debug_pc
    }
    when(instInfo.fuop === cp0_break) {
      except.en     := true.B
      except.excode := ex_Bp
      except.pc     := input.debug_pc
    }
    when(instInfo.fuop === cp0_eret) {
      except.eret := true.B
    }
  }
  when(jmp.isex) {
    except.en       := true.B
    except.excode   := ex_AdEL
    except.pc       := jmp.out.jwaddr
    except.badvaddr := jmp.out.jwaddr
  }
  when(decoder.isex) {
    except.en     := true.B
    except.excode := ex_RI
  }

  // control request
  val isLoadHazard = io.exeDHazard.isload &&
    (io.exeDHazard.waddr === reg.rsaddr ||
      io.exeDHazard.waddr === reg.rtaddr)
  io.ctrlreq.block := isLoadHazard
  io.ctrlreq.clear := false.B
  io.in.ready      := io.out.ready
  io.out.valid     := io.in.valid

  output.exInfo   := except
  output.slot     := input.slot
  output.inst     := decoder.instInfo
  output.op1      := rsdata
  output.op2      := rtdata
  output.pc       := input.pc
  output.binfo    := bpu.binfo
  output.debug_pc := input.debug_pc
  output.rsaddr   := decoder.rsaddr
  output.rtaddr   := decoder.rtaddr
}
