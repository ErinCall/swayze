package st.emily.swayze.tests

import org.scalatest._

import st.emily.swayze.SwayzeApp


abstract class SwayzeSpec
  extends FunSpec
  with ShouldMatchers
  with OptionValues
  with Inside
  with Inspectors

class ApplicationSpec extends SwayzeSpec {
  describe("Running tests") {
    it("should run a mock test") {
      1.should(be(1))
    }
  }
}
