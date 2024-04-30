package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._

trait Cp0RefList {
  val idBadVAddr = 8.U
  val idCount    = 9.U
  val idCompare  = 11.U
  val idStatus   = 12.U
  val idCause    = 13.U
  val idEPC      = 14.U
  val idConfig   = 16.U
}

class CP0 extends Module with Cp0RefList {
  val io = IO(new Bundle {
    val inst  = Input(new InstInfoExt)
    val data  = Input(UInt(DATA_WIDTH.W))
    val wdata = Output(UInt(CP0_WIDTH.W))
  })
  val rBadVAddr = RegInit(0.U(CP0_WIDTH.W))
  val rCount    = RegInit(0.U(CP0_WIDTH.W))
  val rCompare  = RegInit(0.U(CP0_WIDTH.W))

  val rStatusInitVal = Wire(new Cp0Status)
  rStatusInitVal     := 0.U.asTypeOf(new Cp0Status)
  rStatusInitVal.Bev := true.B
  val rStatus = RegInit(rStatusInitVal)

  val rCause   = RegInit(0.U(CP0_WIDTH.W).asTypeOf(new Cp0Cause))
  val rEPC     = RegInit(0.U(CP0_WIDTH.W))
  val rConfig  = RegInit(0.U(CP0_WIDTH.W))
  val rConfig1 = RegInit(0.U(CP0_WIDTH.W))

  val sel = io.inst.imm(2, 0)
  io.wdata := MuxLookup(
    io.inst.rd,
    0.U,
    Seq(
      idBadVAddr -> rBadVAddr.asUInt,
      idCount    -> rCount.asUInt,
      idCompare  -> rCompare.asUInt,
      idStatus   -> rStatus.asUInt,
      idEPC      -> rEPC.asUInt,
      idConfig -> Mux(
        sel === 0.U,
        rConfig.asUInt,
        rConfig1.asUInt,
      ),
    ),
  )

  val en = io.inst.fu === fu_pri && io.inst.fuop === pri_mtc0
  when(en) {
    when(io.inst.rd === idBadVAddr) {
      rBadVAddr := io.data.asTypeOf(rBadVAddr)
    }
    when(io.inst.rd === idCount) {
      rCount := io.data.asTypeOf(rCount)
    }
    when(io.inst.rd === idCompare) {
      rCompare := io.data.asTypeOf(rCompare)
    }
    when(io.inst.rd === idStatus) {
      rStatus := io.data.asTypeOf(rStatus)
    }
    when(io.inst.rd === idEPC) {
      rEPC := io.data.asTypeOf(rEPC)
    }
    when(io.inst.rd === idConfig) {
      when(sel === 0.U) {
        rConfig := io.data.asTypeOf(rConfig)
      }.otherwise {
        rConfig1 := io.data.asTypeOf(rConfig1)
      }
    }
  }
}
