package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._
import cpu.core.pipeline.components.execute._

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val dCache = new Bundle {
      val sram_en    = Output(Bool())
      val sram_wen   = Output(UInt(WEN_WIDTH.W))
      val sram_addr  = Output(UInt(ADDR_WIDTH.W))
      val sram_wdata = Output(UInt(DATA_WIDTH.W))
    }
    val binfo   = Output(new BraInfo)
    val dHazard = Output(new DataHazardExe)
    val in      = new KeepFlushIO(new StageDecodeExecute)
    val out     = new StageExecuteMemory
    val ctrlreq = Output(new CtrlRequest)
  })

  val alu    = Module(new ALU).io
  val muldiv = Module(new MulDiv).io
  val hilo   = Module(new Hilo).io
  val bra    = Module(new BranchCtrl).io

  val input  = io.in.bits
  val output = io.out

  input.rs   <> alu.rs
  input.rt   <> alu.rt
  input.inst <> alu.inst

  input.rs   <> muldiv.rs
  input.rt   <> muldiv.rt
  input.inst <> muldiv.inst

  input.rs   <> bra.rs
  input.rt   <> bra.rt
  input.inst <> bra.inst
  input.pc   <> bra.pc

  input.rs   <> muldiv.rs
  input.rt   <> muldiv.rt
  input.inst <> muldiv.inst

  input.rs     <> hilo.movdata
  input.inst   <> hilo.inst
  muldiv.wen   <> hilo.wen
  muldiv.wdata <> hilo.wdata

  bra.binfo.bwen   <> io.binfo.bwen
  bra.binfo.bwaddr <> io.binfo.bwaddr

  // data select
  val data = MuxLookup(
    input.inst.fu,
    input.inst.imm,
    Seq(
      fu_alu -> alu.out,
      fu_mem -> input.rt,
      fu_mov -> MuxLookup(
        input.inst.fuop,
        0.U,
        Seq(
          mov_mfhi -> hilo.hi,
          mov_mflo -> hilo.lo,
        ),
      ),
      fu_jmp -> (input.pc + 4.U),
      fu_bra -> (input.pc + 4.U),
    ),
  )
  output.data := data

  // TODO: 信号处理未完成
  // mem request
  val vaddr   = input.rs + input.inst.imm
  val memByte = vaddr(1, 0)
  output.memByte      := memByte
  io.dCache.sram_en   := input.inst.fu === fu_mem
  io.dCache.sram_addr := vaddr
  io.dCache.sram_wen := Mux(
    input.inst.wb,
    "b0000".U,
    MuxLookup(
      input.inst.fuop,
      0.U,
      Seq(
        mem_sb -> MuxLookup(
          memByte,
          0.U,
          Seq(
            "b11".U -> "b1000".U,
            "b10".U -> "b0100".U,
            "b01".U -> "b0010".U,
            "b00".U -> "b0001".U,
          ),
        ),
        mem_sh -> Mux(
          memByte(1).asBool,
          "b1100".U,
          "b0011".U,
        ),
        mem_sw -> "b1111".U,
        mem_swl -> MuxLookup(
          memByte,
          0.U,
          Seq(
            "b00".U -> "b0001".U,
            "b01".U -> "b0011".U,
            "b10".U -> "b0111".U,
            "b11".U -> "b1111".U,
          ),
        ),
        mem_swr -> MuxLookup(
          memByte,
          0.U,
          Seq(
            "b00".U -> "b1111".U,
            "b01".U -> "b1110".U,
            "b10".U -> "b1100".U,
            "b11".U -> "b1000".U,
          ),
        ),
      ),
    ),
  )
  io.dCache.sram_wdata := MuxLookup(
    input.inst.fuop,
    input.rt,
    Seq(
      mem_sb -> Fill(4, input.rt(7, 0)),
      mem_sh -> Fill(2, input.rt(15, 0)),
      mem_swl -> MuxLookup(
        memByte,
        0.U,
        Seq(
          "b00".U -> Cat(0.U(24.W), input.rt(31, 24)),
          "b01".U -> Cat(0.U(16.W), input.rt(31, 16)),
          "b10".U -> Cat(0.U(8.W), input.rt(31, 8)),
          "b11".U -> input.rt,
        ),
      ),
      mem_swr -> MuxLookup(
        memByte,
        0.U,
        Seq(
          "b00".U -> input.rt,
          "b01".U -> Cat(input.rt(23, 0), 0.U(8.W)),
          "b10".U -> Cat(input.rt(15, 0), 0.U(16.W)),
          "b11".U -> Cat(input.rt(7, 0), 0.U(24.W)),
        ),
      ),
    ),
  )

  io.dHazard.wen    := input.inst.wb
  io.dHazard.waddr  := input.inst.rd
  io.dHazard.wdata  := data
  io.dHazard.isload := input.inst.fu === fu_mem && input.inst.wb

  // TODO: 创建使能信号对应的临时变量，然后全部或起来
  // 通过这种方式避免选择器嵌套，实现并行控制信号选择
  io.ctrlreq.keep := MuxCase(
    "b00000".U,
    Seq(
      muldiv.block   -> "b11100".U,
      bra.binfo.bwen -> "b00000".U,
    ),
  )
  io.ctrlreq.flush := MuxCase(
    "b00000".U,
    Seq(
      muldiv.block   -> "b00010".U,
      bra.binfo.bwen -> "b01000".U,
    ),
  )

  output.inst.fu   := input.inst.fu
  output.inst.fuop := input.inst.fuop
  output.inst.rd   := input.inst.rd
  output.inst.wb   := input.inst.wb
  output.pc        := input.pc
  output.debug_pc  := input.debug_pc
}
