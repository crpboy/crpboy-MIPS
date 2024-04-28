package cpu.core.pipeline.components.execute

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.common.Const._
import cpu.utils.Functions._

// assume that $rs is divided by $rt
class SignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())

    // rt
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(DATA_WIDTH.W))

    // rs
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(DATA_WIDTH.W))

    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO_WIDTH.W))
  })
}

class UnsignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())

    // rt
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(DATA_WIDTH.W))

    // rs
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(DATA_WIDTH.W))

    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO_WIDTH.W))
  })
}

class Div extends Module with Config {
  val io = IO(new Bundle {
    val en       = Input(Bool())
    val rs       = Input(UInt(DATA_WIDTH.W))
    val rt       = Input(UInt(DATA_WIDTH.W))
    val isSigned = Input(Bool())
    val ready    = Output(Bool())
    val wdata    = Output(UInt(HILO_WIDTH.W))
  })

  if (isBuild) {
    val sDiv = Module(new SignedDiv).io
    val uDiv = Module(new UnsignedDiv).io

    sDiv.aclk                  <> clock
    sDiv.s_axis_dividend_tdata <> io.rs
    sDiv.s_axis_divisor_tdata  <> io.rt
    uDiv.aclk                  <> clock
    uDiv.s_axis_dividend_tdata <> io.rs
    uDiv.s_axis_divisor_tdata  <> io.rt

    val sDividendSent = RegInit(false.B)
    val sDivisorSent  = RegInit(false.B)
    when(io.en && sDiv.s_axis_dividend_tready && sDiv.s_axis_dividend_tvalid) {
      sDividendSent := true.B
    }.elsewhen(io.ready) {
      sDividendSent := false.B
    }
    when(io.en && sDiv.s_axis_divisor_tready && sDiv.s_axis_divisor_tvalid) {
      sDivisorSent := true.B
    }.elsewhen(io.ready) {
      sDivisorSent := false.B
    }

    val uDividendSent = RegInit(false.B)
    val uDivisorSent  = RegInit(false.B)
    when(io.en && uDiv.s_axis_dividend_tready && uDiv.s_axis_dividend_tvalid) {
      uDividendSent := true.B
    }.elsewhen(io.ready) {
      uDividendSent := false.B
    }
    when(io.en && uDiv.s_axis_divisor_tready && uDiv.s_axis_divisor_tvalid) {
      uDivisorSent := true.B
    }.elsewhen(io.ready) {
      uDivisorSent := false.B
    }

    sDiv.s_axis_dividend_tvalid := io.en && !sDividendSent
    sDiv.s_axis_divisor_tvalid  := io.en && !sDivisorSent
    uDiv.s_axis_dividend_tvalid := io.en && !uDividendSent
    uDiv.s_axis_divisor_tvalid  := io.en && !uDivisorSent

    val sReady = RegNext(sDiv.m_axis_dout_tvalid)
    val uReady = RegNext(uDiv.m_axis_dout_tvalid)
    val sRes   = Cat(sDiv.m_axis_dout_tdata(DATA_WIDTH - 1, 0), sDiv.m_axis_dout_tdata(HILO_WIDTH - 1, DATA_WIDTH))
    val uRes   = Cat(uDiv.m_axis_dout_tdata(DATA_WIDTH - 1, 0), uDiv.m_axis_dout_tdata(HILO_WIDTH - 1, DATA_WIDTH))
    io.ready := Mux(io.isSigned, sReady, uReady)
    io.wdata := Mux(io.isSigned, sRes, uRes)
  } else {
    val cnt = RegInit(0.U(log2Ceil(divClockNum + 1).W))
    cnt := Mux(io.en && !io.ready, cnt + 1.U, 0.U)

    val dividend_signed = io.rs(31) & io.isSigned
    val divisor_signed  = io.rt(31) & io.isSigned

    val dividend_abs = Mux(dividend_signed, (-io.rs).asUInt, io.rs.asUInt)
    val divisor_abs  = Mux(divisor_signed, (-io.rt).asUInt, io.rt.asUInt)

    val quotient_signed  = (io.rs(31) ^ io.rt(31)) & io.isSigned
    val remainder_signed = io.rs(31) & io.isSigned

    val quotient_abs  = dividend_abs / divisor_abs
    val remainder_abs = dividend_abs - quotient_abs * divisor_abs

    val quotient  = RegInit(0.S(DATA_WIDTH.W))
    val remainder = RegInit(0.S(DATA_WIDTH.W))

    when(io.en) {
      quotient  := Mux(quotient_signed, (-quotient_abs).asSInt, quotient_abs.asSInt)
      remainder := Mux(remainder_signed, (-remainder_abs).asSInt, remainder_abs.asSInt)
    }

    io.ready := cnt >= divClockNum.U
    io.wdata := Cat(remainder, quotient)
  }
}
