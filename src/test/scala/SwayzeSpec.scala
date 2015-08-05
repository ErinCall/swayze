package st.emily.swayze.test

import org.scalatest._

import st.emily.swayze.SwayzeApp


trait SwayzeSpec
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
