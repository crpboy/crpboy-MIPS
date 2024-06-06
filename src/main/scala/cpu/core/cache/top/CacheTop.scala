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
  val buffer = Module(new WriteBuffer).io
  io.iCache     <> iCache.core
  io.dCache     <> dCache.core
  dCache.buffer <> buffer.dCache

  val state = RegInit(sIdle)
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

  val instWorking = state === sInst
  val dataWorking = state === sData

  io.axi.ar.bits := Mux(instWorking, iCache.axi.ar.bits, dCache.axi.ar.bits)
  io.axi.r.bits  <> iCache.axi.r.bits
  io.axi.r.bits  <> dCache.axi.r.bits

  iCache.axi.r.valid := Mux(instWorking, io.axi.r.valid, false.B)
  dCache.axi.r.valid := Mux(dataWorking, io.axi.r.valid, false.B)

  iCache.axi.ar.ready := Mux(instWorking, io.axi.ar.ready, false.B)
  dCache.axi.ar.ready := Mux(dataWorking, io.axi.ar.ready, false.B)

  io.axi.r.ready := MuxCase(
    false.B,
    Seq(
      instWorking -> iCache.axi.r.ready,
      dataWorking -> dCache.axi.r.ready,
    ),
  )
  io.axi.ar.valid := MuxCase(
    false.B,
    Seq(
      instWorking -> iCache.axi.ar.valid,
      dataWorking -> dCache.axi.ar.valid,
    ),
  )

  io.axi.aw <> buffer.axi.aw
  io.axi.w  <> buffer.axi.w
  io.axi.b  <> buffer.axi.b
}
