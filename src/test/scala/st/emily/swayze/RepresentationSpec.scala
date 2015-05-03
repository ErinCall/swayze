package st.emily.swayze.representation

import st.emily.swayze.tests.SwayzeSpec


class RepresentationSpec extends SwayzeSpec {
  "A NetworkConfiguration" - {
    "given some network name" - {
      "should return a URI-safe name" in {
        val config = NetworkConfiguration(name     = "Some Network",
                                          host     = "irc.example.com",
                                          port     = 6667,
                                          encoding = "UTF-8",
                                          channels = List(),
                                          modules  = List(),
                                          nickname = "nick")

        config.uriSafeName should be ("some-network")
      }
    }
  }
}
