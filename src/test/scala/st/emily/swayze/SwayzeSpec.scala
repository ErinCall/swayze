package st.emily.swayze.tests

import org.scalatest._

import st.emily.swayze.SwayzeApp


abstract class SwayzeSpec
extends FreeSpec
with ShouldMatchers
with OptionValues
with Inside
with Inspectors
with GivenWhenThen

class ApplicationSpec extends SwayzeSpec {
  "Running tests" - {
    "should run a mock test" in {
      1 should be (1)
    }
  }
}
