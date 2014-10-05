package st.emily.swayze.representation

import com.simple.simplespec.Spec
import org.junit.Test


class RepresentationSpec extends Spec {
  class `Configuration tests` {
    @Test def `Loads URI safe name for a network` = {
      val config = NetworkConfiguration(name     = "Some Network",
                                        host     = "irc.example.com",
                                        port     = 6667,
                                        channels = List(),
                                        modules  = List())

      config.uriSafeName.must(be("some-network"))
    }
  }
}
