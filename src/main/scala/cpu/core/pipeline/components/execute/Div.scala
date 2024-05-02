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

    val dividendSigned = io.rs(31) & io.isSigned
    val divisorSgned   = io.rt(31) & io.isSigned

    val dividendAbs = Mux(dividendSigned, (-io.rs).asUInt, io.rs.asUInt)
    val divisorAbs  = Mux(divisorSgned, (-io.rt).asUInt, io.rt.asUInt)

    val quotientSigned  = (io.rs(31) ^ io.rt(31)) & io.isSigned
    val remainderSigned = io.rs(31) & io.isSigned

    val quotientAbs  = dividendAbs / divisorAbs
    val remainderAbs = dividendAbs - quotientAbs * divisorAbs

    val quotient  = RegInit(0.S(DATA_WIDTH.W))
    val remainder = RegInit(0.S(DATA_WIDTH.W))

    when(io.en) {
      quotient  := Mux(quotientSigned, (-quotientAbs).asSInt, quotientAbs.asSInt)
      remainder := Mux(remainderSigned, (-remainderAbs).asSInt, remainderAbs.asSInt)
    }

    io.ready := cnt >= divClockNum.U
    io.wdata := Cat(remainder, quotient)
  }
}
