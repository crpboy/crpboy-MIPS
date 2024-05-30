package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

// don't change order
trait DCacheStateTable {
  val (sIdle ::
    // read uncached
    sruAddr ::
    sruData ::
    sruWait ::
    // read cached
    srcWait ::
    // write uncached
    swuBoth ::
    swuAddr ::
    swuData ::
    swuWait ::
    swuWaitCore ::
    // write cached
    swcWait ::
    Nil) = Enum(11)
}

class DCache extends Module with DCacheStateTable {
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

  val req     = io.core.req.info
  val isRead  = req.valid && !req.wen
  val isWrite = req.valid && req.wen
  switch(state) {
    is(sIdle) {
      when(isRead) {
        stall := true.B
        state := sruAddr
      }.otherwise {
        working := false.B
        when(isWrite) {
          stall := true.B
          state := swuBoth
        }
      }
    }

    // read
    is(sruAddr) {
      arvalid := true.B
      stall   := true.B
      when(ar.ready) {
        state := sruData
      }
    }
    is(sruData) {
      when(r.valid) {
        when(io.core.coreReady) {
          state := sIdle
        }.otherwise {
          dataTmp := r.bits.data
          state   := sruWait
        }
      }.otherwise {
        stall := true.B
      }
    }
    is(sruWait) {
      working := false.B
      memData := dataTmp
      when(io.core.coreReady) {
        state := sIdle
      }
    }

    // write
    is(swuBoth) {
      awvalid := true.B
      wvalid  := true.B
      when(aw.ready && w.ready) {
        state := sIdle
      }.otherwise {
        stall := true.B
        when(aw.ready) {
          state := swuData
        }.elsewhen(w.ready) {
          state := swuAddr
        }
      }
    }
    is(swuData) {
      wvalid := true.B
      when(w.ready) {
        state := swuWait
      }.otherwise {
        stall := true.B
      }
    }
    is(swuAddr) {
      awvalid := true.B
      stall   := true.B
      when(aw.ready) {
        state := swuWait
      }
    }
    is(swuWait) {
      stall := true.B
      when(b.valid) {
        state := sIdle
        // when(io.core.coreReady) {
        //   state := sIdle
        // }.otherwise {
        //   state := swuWaitCore
        // }
      }
    }
  //   is(swuWaitCore) {
  //     stall := true.B
  //     when(io.core.coreReady) {
  //       state := sIdle
  //     }
  //   }
  }

  when(state >= swuBoth) {
    working := false.B
  }

  // cache <> core (exe, mem)
  io.working        := working
  io.core.stall     := stall
  io.core.resp.data := memData

  // axi (ar)
  ar.bits.id    := 1.U
  ar.bits.len   := 0.U
  ar.bits.burst := "b01".U
  ar.bits.lock  := 0.U
  ar.bits.prot  := 0.U
  ar.bits.cache := 0.U
  ar.bits.size  := req.size
  ar.bits.addr  := req.addr

  // axi (aw)
  aw.bits.id    := 1.U
  aw.bits.len   := 0.U
  aw.bits.burst := "b01".U
  aw.bits.lock  := 0.U
  aw.bits.prot  := 0.U
  aw.bits.cache := 0.U
  aw.bits.size  := req.size
  aw.bits.addr  := req.addr

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
