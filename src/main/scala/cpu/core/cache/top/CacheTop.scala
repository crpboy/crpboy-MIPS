package cpu.core.cache.top

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.core.cache.components._

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

  val select = !dataWorking
  io.axi.ar.bits := Mux(select, iCache.axi.ar.bits, dCache.axi.ar.bits)
  io.axi.r.bits  <> iCache.axi.r.bits
  io.axi.r.bits  <> dCache.axi.r.bits

  iCache.axi.r.valid := Mux(select, io.axi.r.valid, false.B)
  dCache.axi.r.valid := Mux(select, false.B, io.axi.r.valid)

  iCache.axi.ar.ready := Mux(select, io.axi.ar.ready, false.B)
  dCache.axi.ar.ready := Mux(select, false.B, io.axi.ar.ready)

  io.axi.r.ready  := Mux(select, iCache.axi.r.ready, dCache.axi.r.ready)
  io.axi.ar.valid := Mux(select, iCache.axi.ar.valid, dCache.axi.ar.valid)

  io.axi.aw <> dCache.axi.aw
  io.axi.w  <> dCache.axi.w
  io.axi.b  <> dCache.axi.b
}
