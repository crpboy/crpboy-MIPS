package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

// don't change order
trait UncachedStateTable {
  val (sIdle ::
    // read
    srAddr ::
    srData ::
    srWait ::
    // write
    swBoth ::
    swAddr ::
    swData ::
    swWait ::
    Nil) = Enum(8)
}

class UncachedUnit extends Module with UncachedStateTable {
  val io = IO(new Bundle {
    val core    = Flipped(new DCacheIO)
    val axi     = new AXI
    val working = Output(Bool())
  })

  val state   = RegInit(sIdle)
  val dataTmp = RegInit(0.U(DATA_WIDTH.W))

  val ar = io.axi.ar
  val aw = io.axi.aw
  val w  = io.axi.w
  val r  = io.axi.r
  val b  = io.axi.b

  val arvalid = WireDefault(false.B)
  val awvalid = WireDefault(false.B)
  val wvalid  = WireDefault(false.B)

  val stall   = WireDefault(false.B)
  val working = WireDefault(true.B)
  val memData = WireDefault(io.axi.r.bits.data)

  val req       = io.core.memory.req
  val isRead    = req.valid && !req.wen
  val isWrite   = req.valid && req.wen
  val unmappped = mmuJudgeUnmapped(req.addr)
  val addr      = Mux(unmappped, Cat(0.U(3.W), req.addr(28, 0)), req.addr)
  val coreReady = io.core.memory.coreReady

  switch(state) {
    is(sIdle) {
      when(isRead) {
        stall := true.B
        state := srAddr
      }.otherwise {
        working := false.B
        when(isWrite) {
          stall := true.B
          state := swBoth
        }
      }
    }

    // read
    is(srAddr) {
      arvalid := true.B
      stall   := true.B
      when(ar.ready) {
        state := srData
      }
    }
    is(srData) {
      when(r.valid) {
        when(coreReady) {
          state := sIdle
        }.otherwise {
          dataTmp := r.bits.data
          state   := srWait
        }
      }.otherwise {
        stall := true.B
      }
    }
    is(srWait) {
      working := false.B
      memData := dataTmp
      when(coreReady) {
        state := sIdle
      }
    }

    // write
    is(swBoth) {
      awvalid := true.B
      wvalid  := true.B
      when(aw.ready && w.ready) {
        state := sIdle
      }.otherwise {
        stall := true.B
        when(aw.ready) {
          state := swData
        }.elsewhen(w.ready) {
          state := swAddr
        }
      }
    }
    is(swData) {
      wvalid := true.B
      when(w.ready) {
        state := swWait
      }.otherwise {
        stall := true.B
      }
    }
    is(swAddr) {
      awvalid := true.B
      stall   := true.B
      when(aw.ready) {
        state := swWait
      }
    }
    is(swWait) {
      stall := true.B
      when(b.valid) {
        state := sIdle
      }
    }
  }

  when(state >= swBoth) {
    working := false.B
  }

  // cache <> core (exe, mem)
  io.working              := working
  io.core.memory.stall    := stall
  io.core.memory.respData := memData

  // axi (ar)
  ar.bits.id    := 1.U
  ar.bits.len   := 0.U
  ar.bits.burst := "b01".U
  ar.bits.lock  := 0.U
  ar.bits.prot  := 0.U
  ar.bits.cache := 0.U
  ar.bits.size  := req.size
  ar.bits.addr  := addr

  // axi (aw)
  aw.bits.id    := 1.U
  aw.bits.len   := 0.U
  aw.bits.burst := "b01".U
  aw.bits.lock  := 0.U
  aw.bits.prot  := 0.U
  aw.bits.cache := 0.U
  aw.bits.size  := req.size
  aw.bits.addr  := addr

  // axi (w)
  w.bits.id   := 1.U
  w.bits.last := 1.U
  w.bits.strb := req.wstrb
  w.bits.data := req.wdata

  // axi (ready & valid)
  io.axi.r.ready  := true.B
  io.axi.b.ready  := true.B
  io.axi.ar.valid := arvalid
  io.axi.aw.valid := awvalid
  io.axi.w.valid  := wvalid
}
