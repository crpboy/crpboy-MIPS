package cpu.core.pipeline.components.decode

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

trait BranchPredictStateTable {
  val sStronglyTaken    = "b10".U
  val sWeaklyTaken      = "b11".U
  val sWeaklyNotTaken   = "b01".U
  val sStronglyNotTaken = "b00".U
}

class BranchPredict extends Module with BranchPredictStateTable {
  val io = IO(new Bundle {
    val ctrl  = Input(new CtrlInfo)
    val pc    = Input(UInt(PC_WIDTH.W))
    val inst  = Input(UInt(INST_WIDTH.W))
    val isb   = Input(Bool())
    val binfo = Output(new BraInfo)
    val bres  = Input(new BraResult) // from exe
  })

  val pht = RegInit(VecInit(Seq.fill(1 << BPU_BHT_WIDTH)(sStronglyTaken)))
  val bht = RegInit(VecInit(Seq.fill(1 << BPU_INDEX_WIDTH)(0.U(BPU_BHT_WIDTH.W))))

  // cnt train
  when(io.bres.isb && !io.ctrl.stall && !io.ctrl.cache.iStall) {
    val bwen   = io.bres.bwen
    val pcHash = io.bres.index

    val phtId = bht(pcHash)
    val state = pht(phtId)

    phtId := bhtUpdate(phtId, bwen)
    switch(state) {
      is(sStronglyTaken) {
        when(bwen) {
          state := sStronglyTaken
        }.otherwise {
          state := sWeaklyTaken
        }
      }
      is(sWeaklyTaken) {
        when(bwen) {
          state := sStronglyTaken
        }.otherwise {
          state := sWeaklyNotTaken
        }
      }
      is(sWeaklyNotTaken) {
        when(bwen) {
          state := sWeaklyTaken
        }.otherwise {
          state := sStronglyNotTaken
        }
      }
      is(sStronglyNotTaken) {
        when(bwen) {
          state := sWeaklyNotTaken
        }.otherwise {
          state := sStronglyNotTaken
        }
      }
    }
  }

  // send prediction
  val pcHash = io.pc(BPU_INDEX_WIDTH + 1, 2)
  val phtId  = bht(pcHash)
  io.binfo.bwen   := Mux(io.isb, (pht(phtId))(1).asBool, false.B)
  io.binfo.bwaddr := io.pc + signedExtend(Cat(io.inst(15, 0), 0.U(2.W)))
}

/*
  11 strongly taken
  10 weakly taken
  01 weakly not taken
  00 strongly not taken
 */
