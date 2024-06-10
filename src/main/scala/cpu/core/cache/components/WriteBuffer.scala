package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

// write dirty cacheline
class WriteBufferReq extends Bundle {
  val addr = UInt(CACHE_NO_OFFSET_WIDTH.W)
  val data = Vec(CACHE_LINE_BYTE_NUM, UInt(BYTE_WIDTH.W))
}
// read from buffer
class WriteBufferReadQueryIO extends Bundle {
  val addr = Input(UInt(CACHE_NO_OFFSET_WIDTH.W))
  val hit  = Output(Bool())
  val data = Output(Vec(CACHE_LINE_BYTE_NUM, UInt(BYTE_WIDTH.W)))
}

// modified from chisel3.util.Queue
// a queue providing read/write hit check
// we can assert that the addr in buffer won't be confilct with cache's
class WriteBufferQueue(val entries: Int) extends Module {
  val io = IO(new Bundle {
    val enq  = Flipped(EnqIO(new WriteBufferReq)) // chisel define
    val deq  = Flipped(DeqIO(new WriteBufferReq)) // chisel define
    val rReq = new WriteBufferReadQueryIO
    val strb = Input(Vec(CACHE_LINE_BYTE_NUM, Bool()))
  })
  // reg def, maybe should apply chisel3.util.SyncMem to data mem?
  def lineType = Vec(CACHE_LINE_BYTE_NUM, UInt(BYTE_WIDTH.W))
  val validMem = Mem(entries, Bool())
  val addrMem  = Mem(entries, UInt(CACHE_NO_OFFSET_WIDTH.W))
  val dataMem  = Mem(entries, lineType)

  // queue pointer (head, tail)
  val enq_ptr = Counter(entries) // tail is mem(enq_ptr - 1), data input here
  val deq_ptr = Counter(entries) // head is mem(deq_ptr), data output here

  // read request (cache -> buffer)
  val readHitVec    = VecInit((0 until entries).map(i => validMem(i) && (addrMem(i) === io.rReq.addr)))
  val readHitSignal = readHitVec.asUInt.orR
  val readHitId     = PriorityEncoder(readHitVec)
  io.rReq.hit  := readHitSignal
  io.rReq.data := dataMem(readHitId)

  // write request
  // it can work whenever queue is full or not
  val writeInfo      = io.enq.bits
  val writeHitVec    = VecInit((0 until entries).map(i => validMem(i) && addrMem(i) === writeInfo.addr))
  val writeHitSignal = writeHitVec.asUInt.orR
  val writeHitId     = PriorityEncoder(writeHitVec)
  val writeEnable    = io.enq.valid && writeHitSignal && !(io.deq.fire && writeHitId === deq_ptr.value)
  when(writeEnable) {
    dataMem.write(writeHitId, writeInfo.data.asTypeOf(lineType), io.strb)
  }

  // full & empty judge
  val maybe_full = RegInit(false.B)
  val ptr_match  = enq_ptr.value === deq_ptr.value
  val empty      = ptr_match && !maybe_full
  val full       = ptr_match && maybe_full

  // push & pop
  val do_enq = io.enq.fire && !writeEnable // push signal (modified)
  val do_deq = io.deq.fire                 // pop signal
  when(do_enq) {
    val id      = enq_ptr.value
    val enqInfo = io.enq.bits
    validMem(id) := true.B
    addrMem(id)  := enqInfo.addr
    dataMem(id)  := enqInfo.data.asTypeOf(dataMem(id))
    enq_ptr.inc()
  }
  when(do_deq) {
    val id = deq_ptr.value
    validMem(id) := false.B
    deq_ptr.inc()
  }
  when(do_enq =/= do_deq) {
    maybe_full := do_enq
  }

  // output
  io.deq.valid := !empty
  io.enq.ready := !full

  val tailId = deq_ptr.value
  io.deq.bits.addr := addrMem(tailId)
  io.deq.bits.data := dataMem(tailId)
}

trait WriteBufferStateTable {
  val (sIdle ::
    sAddr ::
    sData ::
    sWait ::
    suwBoth ::
    suwAddr ::
    suwData ::
    suwWait ::
    Nil) = Enum(8)
}
class UncachedWriteBufferReq extends Bundle {
  val addr = UInt(ADDR_WIDTH.W)
  val data = UInt(DATA_WIDTH.W)
  val strb = UInt(AXI_STRB_WIDTH.W)
}
class WriteBufferDCacheIO extends Bundle {
  val wReq  = Flipped(Decoupled(new WriteBufferReq))
  val uwReq = Flipped(Decoupled())
  val strb  = Input(Vec(CACHE_LINE_BYTE_NUM, Bool()))
  val rReq  = new WriteBufferReadQueryIO
}
// only for cache line write
class WriteBuffer extends Module with WriteBufferStateTable {
  val io = IO(new Bundle {
    val dCache = new WriteBufferDCacheIO
    val axi    = new AXIWrite
  })
  // queue define
  val queue    = Module(new WriteBufferQueue(CACHE_BUFFER_DEPTH)).io
  val uQueue   = Module(new Queue(new UncachedWriteBufferReq, CACHE_UNCACHED_BUFFER_DEPTH)).io
  val deqReady = WireDefault(false.B)
  queue.enq       <> io.dCache.wReq
  queue.strb      <> io.dCache.strb
  queue.rReq      <> io.dCache.rReq
  queue.deq.ready := deqReady

  // axi signal
  val awvalid = WireDefault(false.B)
  val wvalid  = WireDefault(false.B)
  val wlast   = WireDefault(false.B)
  val aw      = io.axi.aw
  val w       = io.axi.w
  val b       = io.axi.b

  // line info
  val addrReg = RegInit(0.U(ADDR_WIDTH.W))
  val lineReg = RegInit(0.U.asTypeOf(new CacheLine))

  // FSM
  val state       = RegInit(sIdle)
  val counter     = RegInit(0.U((log2Ceil(CACHE_BANK_NUM)).W))
  val counterNext = counter + 1.U
  switch(state) {
    is(sIdle) {
      deqReady := true.B
      when(queue.deq.fire) {
        addrReg := queue.deq.bits.addr
        lineReg := queue.deq.bits.data.asTypeOf(lineReg)
        state   := sAddr
      }
    }
    is(sAddr) {
      awvalid := true.B
      when(aw.ready) {
        state   := sData
        counter := 0.U
      }
    }
    is(sData) {
      wvalid := true.B
      when(w.ready) {
        counter := counterNext
        when(counterNext === 0.U) {
          wlast := true.B
          state := sWait
        }
      }
    }
    is(sWait) {
      when(b.valid) {
        state := sIdle
      }
    }
  }

  // axi (aw)
  aw.bits.id    := 1.U
  aw.bits.len   := (CACHE_BANK_NUM - 1).U
  aw.bits.burst := 1.U
  aw.bits.lock  := 0.U
  aw.bits.prot  := 0.U
  aw.bits.cache := 0.U
  aw.bits.size  := 2.U
  aw.bits.addr  := addrReg
  aw.valid      := awvalid

  // axi (w)
  w.bits.id   := 1.U
  w.bits.last := wlast
  w.bits.strb := "b1111".U
  w.bits.data := lineReg.bits(counter)
  w.valid     := wvalid

  // axi (b)
  io.axi.b.ready := true.B
}
