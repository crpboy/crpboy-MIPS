import cpu._
import circt.stage._

object Elaborate extends App {
  def top       = new mycpu_top()
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
  (new ChiselStage)
    .execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.SystemVerilog))
}
