package cpu.core.cache.components

import chisel3._
import chisel3.util._
import cpu.common.const._
import cpu.common.bundles._
import cpu.common.const.Const._
import cpu.utils.Functions._

trait DCacheStateTable {
  val (sIdle ::
    sRead0 ::
    sRead1 ::
    sWriteBack ::
    sReplace ::
    sPause :: // after write
    Nil) = Enum(6)
}

// only for axi read
class DCache extends Module with DCacheStateTable {
  val io = IO(new Bundle {
    val core    = Flipped(new DCacheIO)
    val axi     = new AXIRead
    val buffer  = Flipped(new WriteBufferDCacheIO)
    val working = Output(Bool())
  })
  val state = RegInit(sIdle)

  // axi define
  val ar = io.axi.ar
  val r  = io.axi.r

  // handshake signal: output
  val arvalid = WireDefault(false.B)
  val awvalid = WireDefault(false.B)
  val wvalid  = WireDefault(false.B)

  // core ctrl signal
  val stall    = WireDefault(false.B)
  val exeStall = WireDefault(false.B)
  val working  = WireDefault(true.B)
  val memData  = WireDefault(io.axi.r.bits.data)

  // signal define
  val exe       = io.core.execute.req
  val mem       = io.core.memory.req
  val memReady  = io.core.memory.coreReady
  val uncached  = getUncached(mem.addr)
  val unmappped = mmuJudgeUnmapped(mem.addr)
  val isRead    = mem.valid && !mem.wen
  val isWrite   = mem.valid && mem.wen

  // pipeline ctrl signal
  val exeAddr = Mux(unmappped, Cat(0.U(3.W), exe.addr(28, 0)), exe.addr)
  val memAddr = Mux(unmappped, Cat(0.U(3.W), mem.addr(28, 0)), mem.addr)
  when(exeAddr === memAddr && isWrite) {
    exeStall := true.B
  }

  // cache
  // note that the data of tagv sram is Cat(v, tag)
  val dataSram = VecInit.fill(CACHE_WAY_NUM)(Module(new ReadWritePortSramMasked(CACHE_LINE_WIDTH, CACHE_LINE_DEPTH)).suggestName("dataSram").io)
  val tagvSram = VecInit.fill(CACHE_WAY_NUM)(Module(new ReadWritePortSram(CACHE_TAG_WIDTH + 1, CACHE_LINE_DEPTH)).suggestName("tagvSram").io)
  val dirtSram = VecInit.fill(CACHE_WAY_NUM)(Module(new ReadWritePortSram(1, CACHE_LINE_DEPTH)).suggestName("dirtSram").io)
  val replace  = RegInit(VecInit(Seq.fill(CACHE_LINE_DEPTH)(false.B)))

  // addr info
  val addrReg = RegInit(0.U(ADDR_WIDTH.W))
  val reqAddr = exeAddr

  val curTag    = cacheGetTag(reqAddr)
  val curIndex  = cacheGetIndex(reqAddr)
  val curOffset = cacheGetOffset(reqAddr)

  val lastTag    = cacheGetTag(addrReg)
  val lastIndex  = cacheGetIndex(addrReg)
  val lastOffset = cacheGetOffset(addrReg)

  // send index (normally exe)
  val sendAddr = Mux(
    state =/= sIdle || !memReady,
    addrReg,
    reqAddr,
  )
  val sendTag    = cacheGetTag(sendAddr)
  val sendIndex  = cacheGetIndex(sendAddr)
  val sendOffset = cacheGetOffset(sendAddr)
  for (i <- 0 until CACHE_WAY_NUM) {
    dataSram(i).raddr := sendIndex
    tagvSram(i).raddr := sendIndex
    dirtSram(i).raddr := sendIndex
  }

  // get strb
  val strb    = WireDefault(0.U.asTypeOf(Vec(CACHE_LINE_BYTE_NUM, Bool())))
  val strbTmp = VecInit((0 until BYTE_NUM).map(i => strb(lastOffset + i.U)))
  for (i <- 0 until log2Ceil(BYTE_NUM)) {
    when(mem.size === i.U) {
      for (j <- 0 until (1 << i)) {
        strbTmp(j) := true.B
      }
    }
  }

  // read / write hit check
  // cache hit check
  val sramRes = WireDefault(VecInit.fill(CACHE_WAY_NUM)(0.U.asTypeOf(new DCacheSramResult)))
  for (i <- 0 until CACHE_WAY_NUM) {
    val tagv = tagvSram(i).rdata
    sramRes(i).data  := dataSram(i).rdata
    sramRes(i).tag   := tagv(tagv.getWidth - 2, 0)
    sramRes(i).valid := tagv(tagv.getWidth - 1)
    sramRes(i).hit   := sramRes(i).tag === lastTag && sramRes(i).valid
    sramRes(i).dirty := dirtSram(i).rdata
  }
  val sramHitVec    = VecInit((0 until CACHE_WAY_WIDTH).map(i => sramRes(i).hit))
  val sramHitSignal = sramHitVec.asUInt.orR
  val cacheSel      = PriorityEncoder(sramHitVec)
  val sramHitData   = sramRes(cacheSel).data
  // queue hit check
  io.buffer.rReq.addr := Cat(lastTag, lastIndex)
  val queueHitSignal = io.buffer.rReq.hit
  val queueHitData   = io.buffer.rReq.data(lastOffset)
  // get read overall hit data
  val hitData    = Mux(sramHitSignal, sramHitData, queueHitData)
  val overallHit = sramHitSignal || queueHitSignal
  // output read hit data
  io.core.memory.respData := hitData

  // write sram
  // basic define
  val replaceSignal  = WireDefault(false.B)
  val counter        = RegInit(0.U((log2Ceil(CACHE_BANK_NUM)).W))
  val lineReg        = RegInit(0.U.asTypeOf(new CacheLine))
  val replaceNotDone = RegInit(false.B)
  val writeSend = WireDefault(VecInit.fill(CACHE_WAY_NUM)({
    val info = Wire(new CacheSramWriteInfo)
    info.valid := true.B
    info.wen   := false.B
    info.dirt  := false.B
    info.index := lastIndex
    info.tag   := lastTag
    info.data  := lineReg.asTypeOf(info.data)
    info.strb  := 0.U.asTypeOf(info.strb)
    info
  }))
  // when hit, update write info
  val replaceWayNum = WireDefault(replace(lastIndex))
  when(replaceSignal || (sramHitSignal && isWrite)) {
    val writeSramSel = replace(lastIndex)
    val writeTarget  = writeSend(writeSramSel)
    writeTarget.wen   := true.B
    writeTarget.valid := true.B
    writeTarget.data  := lineReg.asTypeOf(writeTarget.data)
    writeTarget.tag   := lastTag
    writeTarget.index := lastIndex
    // judge: replace whole line or write single data
    when(replaceSignal) {
      writeTarget.dirt := false.B
      writeTarget.strb := Fill(writeTarget.strb.getWidth, true.B)
      when(replaceNotDone) {
        replace(lastIndex) := !replace(lastIndex)
        replaceNotDone     := false.B
      }
    }.otherwise {
      writeTarget.dirt := true.B
      writeTarget.strb := strb.asTypeOf(writeTarget.strb)
    }
  }
  // send write info to sram
  for (i <- 0 until CACHE_WAY_NUM) {
    // dataSram
    dataSram(i).waddr := writeSend(i).index
    dataSram(i).wen   := writeSend(i).wen
    dataSram(i).wdata := writeSend(i).data
    dataSram(i).wstrb := writeSend(i).strb
    // tagvSram
    tagvSram(i).waddr := writeSend(i).index
    tagvSram(i).wen   := writeSend(i).wen
    tagvSram(i).wdata := Cat(writeSend(i).valid, writeSend(i).tag)
    // dirtSram
    dirtSram(i).waddr := writeSend(i).index
    dirtSram(i).wen   := writeSend(i).wen
    dirtSram(i).wdata := writeSend(i).dirt
  }
  // send write info to buffer
  // buffer
  val bufferValid = WireDefault(false.B)
  val bufferData  = sramRes(replaceWayNum).data
  val wReqInfo    = io.buffer.wReq
  wReqInfo.bits.addr := Cat(lastTag, lastIndex)
  io.buffer.strb     := strb
  wReqInfo.valid     := bufferValid
  wReqInfo.bits.data := bufferData.asTypeOf(wReqInfo.bits.data)

  switch(state) {
    is(sIdle) {
      working := false.B
      when(!overallHit) {
        stall := true.B
        state := sRead0
      }
    }
    is(sRead0) {
      stall   := true.B
      arvalid := true.B
      when(ar.ready) {
        counter := 0.U
        state   := sRead1
      }
    }
    is(sRead1) {
      stall := true.B
      when(r.valid) {
        lineReg.write(counter, r.bits.data)
        counter := counter + 1.U
        when(r.bits.last) {
          replaceNotDone := true.B
          when(sramRes(replaceWayNum).dirty) {
            state := sWriteBack
          }
        }
      }
    }
    is(sWriteBack) {
      stall       := true.B
      bufferValid := true.B
      when(io.buffer.wReq.fire || io.buffer.writeDone) {
        state := sReplace
      }
    }
    is(sReplace) {
      stall         := true.B
      replaceSignal := true.B
      state         := sPause
    }
    is(sPause) {
      stall := true.B
      state := sIdle
    }
  }

  // cache <> core (exe, mem)
  io.working              := working
  io.core.execute.stall   := exeStall
  io.core.memory.stall    := stall
  io.core.memory.respData := memData

  // axi (ar)
  ar.bits.id      := 0.U
  ar.bits.len     := (CACHE_BANK_NUM - 1).U
  ar.bits.burst   := 1.U
  ar.bits.lock    := 0.U
  ar.bits.prot    := 0.U
  ar.bits.cache   := 0.U
  ar.bits.size    := 2.U
  ar.bits.addr    := Cat(lastTag, lastIndex, 0.U(lastOffset.getWidth.W))
  io.axi.ar.valid := arvalid

  // axi (ready & valid)
  io.axi.r.ready := true.B
}
/*

1. write入队写缓存时，判断队列当中是否存在相同addr，若有，直接写入
2. read判断cachehit时，需要向写缓存发出读请求，从写缓存当中读出是否有重复地址（这个操作是全相连的）
3. 队列满的时候，如果也有write入队写缓存，需要等

====================================================

来讨论一下data sram在实现dcahce下的行为

方案1 (discard)
全部放到mem，共用同个端口，写后读阻塞两拍

read:
  exe发送addr到tagv sram
  mem接收tag 判断hit 发送读请求(addr, data)
  wb接收读返回数据
write:
  exe发送addr到tagv sram
  mem接收tag 判断hit 发送写请求(addr, data)
  wb写成功

tagvSram和dataSram的地址信号不一致，dataSram延后一拍
read和write都在mem发送真正的读dataSram请求
read的前递信息在wb阶段在能够获取
前递时，decode需要一直阻塞，直到read指令流到wb

这个方案的优点是，read和write的请求完全同步(exe -> tag, mem -> (addr, data))
这样的话就不需要经过判地址+数据前递了
并且这个方案使用的是单个读写端口的sram (这真的有意义吗)
缺点是前递需要阻塞exe, mem两拍时间，而原本使用sram的时候只需要一拍即可
这个影响非常大，毕竟很多时候load完就会马上使用load的数据，多停一拍不太能接受

方案2 (wip)

read:
  请求在exe发出，同时发送给tagvSram和dataSram
  在mem直接判断是否hit，并获得dataSram的数据
write:
  同方案1
  exe发送addr到tagv sram
  mem接收tag 判断hit 发送写请求(addr, data)
  wb写成功

这个方案，由于read请求发生在exe，write请求发生在mem
所以需要独立的read/write两个端口
原来的单个读写端口的sram写法没法用了，需要进一步的改进

====================================================

然后讨论一下写缓存

写缓存将使用队列实现
我打算只对于cacheline进行写缓存
暂且就全部视为cached吧，假如后面还需要实现uncached操作我再改

每次发生dirty行替换的时候
都会进行一次队列的push操作
然后由这个队列进行行信息的axi交互

 */
