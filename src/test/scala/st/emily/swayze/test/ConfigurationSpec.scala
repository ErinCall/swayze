package st.emily.swayze.test

import com.typesafe.config.ConfigFactory

import st.emily.swayze.data._


class ConfigurationSpec extends SwayzeSpec {
  "A SwayzeConfig" - {
    "parses HOCON into some network configurations" in {
      val text =
        """
        swayze {
          networks = [
            {
              name     = Some Network
              host     = irc.example.com
              port     = 6667
              encoding = UTF-8
              channels = [ "#channel" ]
              modules  = []
              nickname = nick
            }
            {
              name     = Another Network
              host     = irc.example.net
              port     = 6667
              encoding = UTF-8
              channels = [ "#room" ]
              modules  = [ "away" ]
              nickname = nick
            }
          ]
        }
        """

      val config = SwayzeConfig(ConfigFactory.parseString(text))
      config.getNetworkConfigs should be (
        List(NetworkConfig(name     = "Some Network",
                           host     = "irc.example.com",
                           port     = 6667,
                           encoding = "UTF-8",
                           channels = List("#channel"),
                           modules  = List(),
                           nickname = "nick"),
             NetworkConfig(name     = "Another Network",
                           host     = "irc.example.net",
                           port     = 6667,
                           encoding = "UTF-8",
                           channels = List("#room"),
                           modules  = List("away"),
                           nickname = "nick"))
       )
    }
  }

  "A NetworkConfig" - {
    "given some network name" - {
      "should return a URI-safe name" in {
        val config = NetworkConfig(name     = "Some Network",
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
