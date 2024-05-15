// package cpu.core.pipeline.components.ctrl

// import chisel3._
// import chisel3.util._
// import cpu.common.const._
// import cpu.common.bundles._
// import cpu.common.const.Const._

// class CacheStallCtrl extends Module {
//   val io = IO(new Bundle {
//     val instStall = Input(Bool())
//     val dataStall = Input(Bool())
//     val stall     = Output(UInt(5.W))
//   })
//   val res = Wire(Vec(5, Bool()))
//   res(4)   := io.instStall // | io.dataStall
//   res(3)   := io.instStall // | io.dataStall
//   res(2)   := io.instStall // | io.dataStall
//   res(1)   := io.instStall // | io.dataStall
//   res(0)   := io.instStall
//   io.stall := res.asTypeOf(io.stall)
// }
