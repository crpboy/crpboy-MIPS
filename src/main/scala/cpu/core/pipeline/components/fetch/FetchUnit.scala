package cpu.core.pipeline.components.fetch

import chisel3._
import chisel3.util._
import cpu.common.Const._
import cpu.core.pipeline.components.fetch._
import cpu.common._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val iCache = new Bundle {
      val inst_sram_rdata = Input(UInt(INST_WIDTH.W))
      val inst_sram_en    = Output(Bool())
      val inst_sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val pcNext          = Output(UInt(PC_WIDTH.W))
    }
    val ctrl  = Input(new CtrlInfo)
    val jinfo = Input(new JmpInfo)
    val binfo = Input(new BraInfo)
    val exinfo = new Bundle {
      val id = Input(new ExInfoDecode)
    }
    val ctrlreq = Output(new CtrlRequest)
    val out     = new StageFetchDecode
  })

  val preDecoder = Module(new PreDecoder).io

  val output = io.out

  val pcReg     = RegNext(io.iCache.pcNext, (PC_INIT_ADDR_SUB.U)(PC_WIDTH.W))
  val pcNextTmp = pcReg + 4.U
  val pcNext = MuxCase(
    pcNextTmp,
    Seq(
      io.ctrl.stall   -> pcReg,
      io.jinfo.jwen   -> io.jinfo.jwaddr,
      io.binfo.bwen   -> io.binfo.bwaddr,
      io.exinfo.id.en -> EX_INIT_ADDR.U,
    ),
  )
  val rst = RegNext(reset)
  io.iCache.inst_sram_rdata <> preDecoder.inst
  io.iCache.pcNext          := pcNext
  io.iCache.inst_sram_addr  := io.iCache.pcNext
  io.iCache.inst_sram_en    := !(reset.asBool)

  io.ctrlreq.block := rst
  io.ctrlreq.clear := false.B

  output.inst     := io.iCache.inst_sram_rdata
  output.pc       := pcNextTmp
  output.debug_pc := pcReg
}
