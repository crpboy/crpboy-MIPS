package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

class BranchPredict extends Module {
  val io = IO(new Bundle {
    val ctrl  = Input(new CtrlInfo)
    val pc    = Input(UInt(PC_WIDTH.W))
    val inst  = Input(UInt(INST_WIDTH.W))
    val isb   = Input(Bool())
    val binfo = Output(new BraInfo)
    val bres  = Input(new BraResult) // from exe
  })
  val predict = RegInit(VecInit(Seq.fill(1 << BPU_HISTORY_WIDTH)("b11".U(BPU_PREDICT_WIDTH.W))))
  val history = RegInit(VecInit(Seq.fill(1 << BPU_INDEX_WIDTH)(0.U(BPU_HISTORY_WIDTH.W))))

  // cnt train
  when(io.bres.isb && !io.ctrl.stall && !io.ctrl.cache.iStall) {
    val bwen = io.bres.bwen
    val cur  = history(io.bres.index)
    val nxt  = Cat(cur(BPU_HISTORY_WIDTH - 2, 0), bwen)
    val data = predict(cur)

    when(bwen) {
      when(data =/= "b11".U) {
        data := data + 1.U
      }
    }.otherwise {
      when(data =/= "b00".U) {
        data := data - 1.U
      }
    }
    cur := nxt
  }

  // send prediction
  val index = io.pc(BPU_INDEX_WIDTH + 1, 2)
  io.binfo.bwen   := Mux(io.isb, (predict(history(index)))(1).asBool, false.B)
  io.binfo.bwaddr := io.pc + signedExtend(Cat(io.inst(15, 0), 0.U(2.W)))
}

/*
  11 strongly taken
  10 weakly taken
  01 weakly not taken
  00 strongly not taken
 */
