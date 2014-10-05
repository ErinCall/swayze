package st.emily.swayze.conf

import com.simple.simplespec.Spec
import org.junit.Test

import st.emily.swayze.representation.NetworkConfiguration


class ConfigurationSpec extends Spec {
  class `Network configuration tests` {
    @Test def `Loads networks` = {
      val text = """
                 swayze {
                   networks = [
                     {
                       name     = Some Network
                       host     = irc.example.com
                       port     = 6667
                       encoding = UTF-8
                       channels = [ "#channel" ]
                       modules  = []
                     }
                     {
                       name     = Another Network
                       host     = irc.example.net
                       port     = 6667
                       encoding = UTF-8
                       channels = [ "#room" ]
                       modules  = [ "away" ]
                     }
                   ]
                 }
                 """

      val config = SwayzeConfig(text)
      config.getNetworkConfigs.must(be(
        List(NetworkConfiguration(name     = "Some Network",
                                  host     = "irc.example.com",
                                  port     = 6667,
                                  encoding = "UTF-8",
                                  channels = List("#channel"),
                                  modules  = List()),
             NetworkConfiguration(name     = "Another Network",
                                  host     = "irc.example.net",
                                  port     = 6667,
                                  encoding = "UTF-8",
                                  channels = List("#room"),
                                  modules  = List("away")))
       ))
    }
  }
}
