circuit CrpboyMips :
  module CrpboyMips :
    input clock : Clock
    input reset : UInt<1>
    input io_a : UInt<2>
    input io_b : UInt<2>
    output io_out : UInt<2>

    node _io_out_T = and(io_a, io_b) @[CrpboyMips.scala 12:24]
    io_out <= _io_out_T @[CrpboyMips.scala 12:16]
