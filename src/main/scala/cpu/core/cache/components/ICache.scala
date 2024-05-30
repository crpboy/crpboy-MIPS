package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

trait ICacheStateTable {
  val (sIdle ::
    // cached
    scReplace0 ::
    scReplace1 ::
    scReplace2 ::
    scWait ::
    // uncached
    suRead0 ::
    suRead1 ::
    suWait ::
    Nil) = Enum(8)
}

class CacheSramInfo extends Bundle {
  val index = UInt(CACHE_INDEX_WIDTH.W)
  val data  = UInt(CACHE_LINE_WIDTH.W)
  val tag   = UInt(CACHE_TAG_WIDTH.W)
  val valid = Bool()
  val wen   = Bool()
}

class CacheSramResult extends Bundle {
  val data  = UInt(CACHE_LINE_WIDTH.W)
  val tag   = UInt(CACHE_TAG_WIDTH.W)
  val valid = Bool()
  val hit   = Bool()
}

class ICache extends Module with ICacheStateTable with Config {
  val io = IO(new Bundle {
    val core    = Flipped(new ICacheIO)
    val axi     = new AXIInst
    val working = Output(Bool())
  })

  // basic reg
  val state   = RegInit(sIdle)
  val addrReg = RegInit(0.U(ADDR_WIDTH.W))

  // uncached reg
  val dataReg = RegInit(0.U(DATA_WIDTH.W))
  val dataTmp = RegInit(0.U(DATA_WIDTH.W))

  // cached reg
  val counter        = RegInit(0.U((log2Ceil(CACHE_BANK_NUM)).W))
  val lineReg        = RegInit(0.U.asTypeOf(new CacheLine))
  val replaceNotDone = RegInit(false.B)

  // axi def
  val ar = io.axi.ar
  val r  = io.axi.r

  // ctrl signal wire
  val arvalid   = WireDefault(false.B)
  val stall     = WireDefault(false.B)
  val working   = WireDefault(true.B)
  val writeSram = WireDefault(false.B)

  // cache def
  val dataSram    = VecInit.fill(CACHE_WAY_NUM)(Module(new xilinx_single_port_ram_read_first(CACHE_LINE_WIDTH, CACHE_LINE_DEPTH)).suggestName("dataSram").io)
  val tagvSram    = VecInit.fill(CACHE_WAY_NUM)(Module(new xilinx_single_port_ram_read_first(CACHE_TAG_WIDTH + 1, CACHE_LINE_DEPTH)).suggestName("tagvSram").io)
  val replace     = RegInit(VecInit(Seq.fill(CACHE_LINE_DEPTH)(false.B)))
  val lastIsCache = RegInit(false.B)

  // get cur & last addr info
  val curTag    = cacheGetTag(io.core.addr)
  val curIndex  = cacheGetIndex(io.core.addr)
  val curOffset = cacheGetOffset(io.core.addr)

  val lastTag    = cacheGetTag(addrReg)
  val lastIndex  = cacheGetIndex(addrReg)
  val lastOffset = cacheGetOffset(addrReg)

  // send to cache
  val sramSend = WireDefault(VecInit.fill(CACHE_WAY_NUM)({
    val info = WireDefault(0.U.asTypeOf(new CacheSramInfo))
    info.index := Mux(
      VecInit(scReplace0, scReplace1, scReplace2, scWait).contains(state) || !io.core.coreReady,
      lastIndex,
      curIndex,
    )
    info.valid := true.B
    info.wen   := false.B
    info
  }))
  for (i <- 0 until CACHE_WAY_NUM) {
    dataSram(i).addra := sramSend(i).index
    dataSram(i).dina  := sramSend(i).data
    dataSram(i).clka  := clock
    dataSram(i).wea   := sramSend(i).wen

    tagvSram(i).addra := sramSend(i).index
    tagvSram(i).dina  := Cat(sramSend(i).valid, sramSend(i).tag)
    tagvSram(i).clka  := clock
    tagvSram(i).wea   := sramSend(i).wen
  }

  // write sram info
  // it is valid when scReplace2/Wait
  when(writeSram) {
    val writeSramSel = replace(lastIndex)
    val sramSendTmp  = sramSend(writeSramSel)
    sramSendTmp.wen   := true.B
    sramSendTmp.data  := lineReg.asTypeOf(sramSendTmp.data)
    sramSendTmp.tag   := lastTag
    sramSendTmp.valid := true.B
    sramSendTmp.index := lastIndex
    when(replaceNotDone) {
      replace(lastIndex) := !replace(lastIndex)
      replaceNotDone     := false.B
    }
  }

  // cache result hit check
  val cacheSel   = WireDefault(0.U(CACHE_WAY_WIDTH.W))
  val sramRes    = WireDefault(VecInit.fill(CACHE_WAY_NUM)(0.U.asTypeOf(new CacheSramResult)))
  val overallHit = WireDefault(false.B)
  for (i <- 0 until CACHE_WAY_NUM) {
    val tagv = tagvSram(i).douta
    sramRes(i).data  := dataSram(i).douta
    sramRes(i).tag   := tagv(tagv.getWidth - 2, 0)
    sramRes(i).valid := tagv(tagv.getWidth - 1)
    sramRes(i).hit   := sramRes(i).tag === lastTag && sramRes(i).valid
    when(sramRes(i).hit) {
      cacheSel   := i.asUInt
      overallHit := true.B
    }
  }

  switch(state) {
    is(sIdle) {
      working := false.B
      when(io.core.valid) {
        when(io.core.uncached) {
          // uncached: idle -> read0
          stall       := true.B
          addrReg     := io.core.addr
          state       := suRead0
          lastIsCache := false.B
        }.elsewhen(!overallHit && lastIsCache) {
          // cache miss: idle -> replace0
          // missing target is the last addr
          stall := true.B
          state := scReplace0
        }.elsewhen(io.core.coreReady) {
          addrReg     := io.core.addr
          lastIsCache := true.B
        }
      }
    }

    // cached
    is(scReplace0) {
      stall   := true.B
      arvalid := true.B
      when(ar.ready) {
        counter := 0.U
        state   := scReplace1
      }
    }
    is(scReplace1) {
      stall := true.B
      when(r.valid) {
        lineReg.write(counter, r.bits.data)
        counter := counter + 1.U
        when(r.bits.last) {
          state          := scReplace2
          replaceNotDone := true.B
        }
      }
    }
    is(scReplace2) {
      // axi read done, but should write sram
      working   := false.B
      stall     := true.B
      writeSram := true.B
      when(io.core.coreReady) {
        state := sIdle
      }.otherwise {
        state := scWait
      }
    }
    is(scWait) {
      working   := false.B
      stall     := false.B
      writeSram := true.B
      when(io.core.coreReady) {
        addrReg     := io.core.addr
        state       := sIdle
        lastIsCache := true.B
      }
    }

    // uncached
    is(suRead0) {
      stall   := true.B
      arvalid := true.B
      when(ar.ready) {
        state := suRead1
      }
    }
    is(suRead1) {
      when(r.valid) {
        when(io.core.addr =/= addrReg) {
          // same as idle -> read0
          stall   := true.B
          addrReg := io.core.addr
          state   := suRead0
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
  val sramDataOut    = sramRes(cacheSel).data.asTypeOf(new CacheLine)
  val sramDataOutTmp = sramDataOut.read(lastOffset(CACHE_OFFSET_WIDTH - 1, 2))
  val ramDataOut     = dataReg
  io.working    := working
  io.core.data  := Mux(lastIsCache, sramDataOutTmp, ramDataOut)
  io.core.stall := stall

  // axi
  val stateUncached = VecInit(suRead0, suRead1, suWait)
  when(stateUncached.contains(state)) {
    ar.bits.id    := 0.U
    ar.bits.len   := 0.U
    ar.bits.burst := 1.U
    ar.bits.lock  := 0.U
    ar.bits.prot  := 0.U
    ar.bits.cache := 0.U
    ar.bits.size  := 2.U
    ar.bits.addr  := addrReg
  }.otherwise {
    ar.bits.id    := 0.U
    ar.bits.len   := (CACHE_BANK_NUM - 1).U
    ar.bits.burst := 1.U
    ar.bits.lock  := 0.U
    ar.bits.prot  := 0.U
    ar.bits.cache := 0.U
    ar.bits.size  := 2.U
    ar.bits.addr  := Cat(lastTag, lastIndex, 0.U(lastOffset.getWidth.W))
  }

  ar.valid := arvalid
  r.ready  := true.B

  if (isDebug) {
    dontTouch(lastTag)
    dontTouch(lastIndex)
    dontTouch(lastOffset)
    dontTouch(curTag)
    dontTouch(curIndex)
    dontTouch(curOffset)
    dontTouch(sramDataOut)
    dontTouch(overallHit)
  }
}
