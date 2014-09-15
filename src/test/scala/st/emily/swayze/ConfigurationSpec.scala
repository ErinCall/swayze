package st.emily.swayze.tests

import com.simple.simplespec.Spec
import org.junit.Test

import st.emily.swayze.conf.SwayzeConfig
import st.emily.swayze.types.NetworkConfiguration


class ConfigurationSpec extends Spec {
  class Config {
    @Test def `Loads networks` = {
      val text = """
                 swayze {
                   networks = [
                     {
                       name     = Some Network
                       host     = irc.example.com
                       port     = 6667
                       channels = [ "#channel" ]
                       modules  = []
                     }
                     {
                       name     = Another Network
                       host     = irc.example.net
                       port     = 6667
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
                                  channels = List("#channel"),
                                  modules  = List()),
             NetworkConfiguration(name     = "Another Network",
                                  host     = "irc.example.net",
                                  port     = 6667,
                                  channels = List("#room"),
                                  modules  = List("away")))
       ))
    }
  }
}

