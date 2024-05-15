package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class CacheTop extends Module {
  val io = IO(new Bundle {
    val axi    = new AXI
    val iCache = Flipped(new ICacheIO)
    val dCache = Flipped(new DCacheIO)
  })
  val sIdle :: sInst :: sData :: Nil = Enum(3)

  val iCache = Module(new ICache).io
  val dCache = Module(new DCache).io
  io.iCache <> iCache.core
  io.dCache <> dCache.core

  val state       = RegInit(sIdle)
  val dataWorking = state === sData

  switch(state) {
    is(sIdle) {
      when(dCache.working) {
        state := sData
      }.elsewhen(iCache.working) {
        state := sInst
      }
    }
    is(sInst) {
      when(!iCache.working) {
        when(dCache.working) {
          state := sData
        }.otherwise {
          state := sIdle
        }
      }
    }
    is(sData) {
      when(!dCache.working) {
        when(iCache.working) {
          state := sInst
        }.otherwise {
          state := sIdle
        }
      }
    }
  }

  io.axi.ar.bits := Mux(
    dataWorking,
    dCache.axi.ar.bits,
    iCache.axi.ar.bits,
  )
  io.axi.r.bits <> iCache.axi.r.bits
  io.axi.r.bits <> dCache.axi.r.bits

  when(dataWorking) {
    iCache.axi.r.valid := false.B
    dCache.axi.r.valid := io.axi.r.valid
    io.axi.ar.valid    := dCache.axi.ar.valid
  }.otherwise {
    iCache.axi.r.valid := io.axi.r.valid
    dCache.axi.r.valid := false.B
    io.axi.ar.valid    := iCache.axi.ar.valid
  }
  io.axi.r.ready := true.B

  io.axi.aw <> dCache.axi.aw
  io.axi.w  <> dCache.axi.w
  io.axi.b  <> dCache.axi.b
}
