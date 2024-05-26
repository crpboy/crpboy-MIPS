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
  val pht = RegInit(VecInit(Seq.fill(1 << BPU_BHT_WIDTH)("b11".U)))
  val bht = RegInit(VecInit(Seq.fill(1 << BPU_INDEX_WIDTH)(0.U(BPU_BHT_WIDTH.W))))

  // cnt train
  when(io.bres.isb && !io.ctrl.stall && !io.ctrl.cache.iStall) {
    val bwen   = io.bres.bwen
    val pcHash = io.bres.index

    val phtId = bht(pcHash)
    val data  = pht(phtId)

    phtId := bhtUpdate(phtId, bwen)
    when(bwen) {
      when(data =/= "b11".U) {
        data := data + 1.U
      }
    }.otherwise {
      when(data =/= "b00".U) {
        data := data - 1.U
      }
    }
  }

  // send prediction
  val pcHash = io.pc(BPU_INDEX_WIDTH + 1, 2)
  val phtId = bht(pcHash)
  io.binfo.bwen   := Mux(io.isb, (pht(phtId))(1).asBool, false.B)
  io.binfo.bwaddr := io.pc + signedExtend(Cat(io.inst(15, 0), 0.U(2.W)))
}

/*
  11 strongly taken
  10 weakly taken
  01 weakly not taken
  00 strongly not taken
 */
