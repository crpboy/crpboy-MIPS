package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class ICacheInterface extends Module {
  val io = IO(new Bundle {
    val core    = Flipped(new ICacheIO)
    val axi     = new AXIInst
    val working = Output(Bool())
  })
  val (sIdle
    :: suRead0
    :: suRead1
    :: suWait
    :: scWait
    :: Nil) = Enum(5)

  val state   = RegInit(sIdle)
  val addrReg = RegInit(0.U(ADDR_WIDTH.W))
  val dataReg = RegInit(0.U(DATA_WIDTH.W))
  val dataTmp = RegInit(0.U(DATA_WIDTH.W))

  val ar = io.axi.ar
  val r  = io.axi.r

  val arvalid = WireDefault(false.B)
  val stall   = WireDefault(false.B)
  val working = WireDefault(true.B)

  switch(state) {
    is(sIdle) {
      working := false.B
      when(io.core.valid) {
        stall   := true.B
        addrReg := io.core.pcNext
        state   := suRead0
      }
    }
    is(suRead0) {
      stall   := true.B
      arvalid := true.B
      when(ar.ready) {
        state := suRead1
      }
    }
    is(suRead1) {
      when(r.valid) {
        when(io.core.pcNext =/= addrReg) {
          stall   := true.B
          addrReg := io.core.pcNext
          state   := suRead0
          // same as idle -> read0
        }.elsewhen(io.core.coreReady) {
          dataReg := r.bits.data
          state   := sIdle
        }.otherwise {
          dataTmp := r.bits.data
          state   := suWait
        }
      }.otherwise {
        stall := true.B
      }
    }
    is(suWait) {
      working := false.B
      when(io.core.coreReady) {
        dataReg := dataTmp
        state   := sIdle
      }
    }
  }

  // cache <> core
  io.working    := working
  io.core.data  := dataReg
  io.core.stall := stall

  // axi
  ar.bits.id    := 0.U
  ar.bits.len   := 0.U
  ar.bits.burst := "b01".U
  ar.bits.lock  := 0.U
  ar.bits.prot  := 0.U
  ar.bits.cache := 0.U
  ar.bits.size  := 2.U
  ar.bits.addr  := addrReg

  ar.valid := arvalid
  r.ready  := true.B
}
