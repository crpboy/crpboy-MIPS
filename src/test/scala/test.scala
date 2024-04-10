import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.experimental.BundleLiterals._
import chisel3.tester._
import chisel3.tester.RawTester.test
import cpu.core.CoreTop


object testMain extends App{
  test{new CoreTop} { c =>
    
  }
}