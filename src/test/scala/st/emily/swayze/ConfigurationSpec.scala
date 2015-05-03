package st.emily.swayze

import com.typesafe.config.ConfigFactory

import st.emily.swayze.tests.SwayzeSpec
import st.emily.swayze.representation.NetworkConfiguration


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
        List(NetworkConfiguration(name     = "Some Network",
                                  host     = "irc.example.com",
                                  port     = 6667,
                                  encoding = "UTF-8",
                                  channels = List("#channel"),
                                  modules  = List(),
                                  nickname = "nick"),
             NetworkConfiguration(name     = "Another Network",
                                  host     = "irc.example.net",
                                  port     = 6667,
                                  encoding = "UTF-8",
                                  channels = List("#room"),
                                  modules  = List("away"),
                                  nickname = "nick"))
       )
    }
  }
}
