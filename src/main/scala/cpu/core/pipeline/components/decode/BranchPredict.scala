package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class BranchPredict extends Module {
  val io = IO(new Bundle {
    val pc    = Input(UInt(PC_WIDTH.W))
    val inst  = Input(UInt(INST_WIDTH.W))
    val isb   = Input(Bool())
    val binfo = Output(new BraInfo)
    val bres  = Input(new BraResult) // from exe
  })
  val cnt   = RegInit(VecInit(Seq.fill(BPU_NUM)(0.U(BPU_CNT_WIDTH.W))))
  val state = RegInit(0.U(BPU_STATE_WIDTH.W))

  // cnt train
  when(io.bres.isb) {
    val bwen      = io.bres.bwen
    val nextState = Cat(state(BPU_STATE_WIDTH - 2, 0), bwen)
    val data      = cnt(nextState)
    when(bwen) {
      when(data =/= "b11".U) {
        data := data + 1.U
      }
    }.otherwise {
      when(data =/= "b00".U) {
        data := data - 1.U
      }
    }
    state := nextState
  }

  // send prediction
  io.binfo.bwen   := Mux(io.isb, (cnt(state).asUInt)(1).asBool, false.B)
  io.binfo.bwaddr := io.pc + signedExtend(Cat(io.inst(15, 0), 0.U(2.W)))
}

/*
  11 strongly taken
  10 weakly taken
  01 weakly not taken
  00 strongly not taken
 */
