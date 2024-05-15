package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._

class DCache extends Module {
  val io = IO(new Bundle {
    val core    = Flipped(new DCacheIO)
    val axi     = new AXI
    val working = Output(Bool())
  })
  val srIdle :: srAddr :: srData :: srWait :: Nil                       = Enum(4)
  val swIdle :: swBoth :: swAddr :: swData :: swWait0 :: swWait1 :: Nil = Enum(6)

  val rstate  = RegInit(srIdle)
  val wstate  = RegInit(swIdle)
  val dataTmp = RegInit(0.U(DATA_WIDTH.W))

  val ar = io.axi.ar
  val aw = io.axi.aw
  val w  = io.axi.w
  val r  = io.axi.r
  val b  = io.axi.b

  val arvalid = WireDefault(false.B)
  val awvalid = WireDefault(false.B)
  val wvalid  = WireDefault(false.B)

  val exeStall = WireDefault(false.B)
  val memStall = WireDefault(false.B)
  val working  = WireDefault(true.B)
  val memData  = WireDefault(io.axi.r.bits.data)

  // read state
  val isRead = io.core.exe.valid && !io.core.exe.wen
  switch(rstate) {
    // execute (send request)
    is(srIdle) {
      when(isRead) {
        exeStall := true.B
        rstate   := srAddr
      }.otherwise {
        working := false.B
      }
    }
    is(srAddr) {
      arvalid := true.B
      when(ar.ready) {
        rstate := srData
      }.otherwise {
        exeStall := true.B
      }
    }
    // memory (recieve data)
    is(srData) {
      when(r.valid) {
        when(io.core.mem.coreReady) {
          rstate := srIdle
        }.otherwise {
          dataTmp := r.bits.data
          rstate  := srWait
        }
      }.otherwise {
        memStall := true.B
      }
    }
    is(srWait) {
      memData := dataTmp
      when(io.core.mem.coreReady) {
        rstate := srIdle
      }
    }
  }

  // write state
  val isWrite = io.core.exe.valid && io.core.exe.wen
  switch(wstate) {
    is(swIdle) {
      when(isWrite) {
        exeStall := true.B
        wstate   := swBoth
      }
    }
    is(swBoth) {
      awvalid := true.B
      wvalid  := true.B
      when(aw.ready && w.ready) {
        wstate := swIdle
      }.otherwise {
        exeStall := true.B
        when(aw.ready) {
          wstate := swData
        }.elsewhen(w.ready) {
          wstate := swAddr
        }
      }
    }
    is(swData) {
      wvalid := true.B
      when(w.ready) {
        wstate := swWait0
      }.otherwise {
        exeStall := true.B
      }
    }
    is(swAddr) {
      awvalid := true.B
      when(aw.ready) {
        wstate := swWait0
      }.otherwise {
        exeStall := true.B
      }
    }
    is(swWait0) {
      when(isWrite) {
        exeStall := true.B
      }
      when(b.valid) {
        wstate := swIdle
      }
    }
  }

  // cache <> core (exe, mem)
  io.working        := working
  io.core.exe.stall := !exeStall
  io.core.mem.stall := !memStall
  io.core.mem.data  := memData

  // axi (ar)
  ar.bits.id    := 1.U
  ar.bits.len   := 0.U
  ar.bits.burst := "b01".U
  ar.bits.lock  := 0.U
  ar.bits.prot  := 0.U
  ar.bits.cache := 0.U
  ar.bits.size  := io.core.exe.size
  ar.bits.addr  := io.core.exe.addr

  // axi (aw)
  aw.bits.id    := 1.U
  aw.bits.len   := 0.U
  aw.bits.burst := "b01".U
  aw.bits.lock  := 0.U
  aw.bits.prot  := 0.U
  aw.bits.cache := 0.U
  aw.bits.size  := io.core.exe.size
  aw.bits.addr  := io.core.exe.addr

  // axi (w)
  w.bits.id   := 1.U
  w.bits.last := 1.U
  w.bits.strb := io.core.exe.wstrb
  w.bits.data := io.core.exe.wdata

  // axi (ready & valid)
  io.axi.r.ready  := true.B
  io.axi.b.ready  := true.B
  io.axi.ar.valid := arvalid
  io.axi.aw.valid := awvalid
  io.axi.w.valid  := wvalid
}
