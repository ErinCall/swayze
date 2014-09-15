package st.emily.swayze.tests

import com.simple.simplespec.Spec
import org.junit.Test
import scala.collection.JavaConversions._

import st.emily.swayze.SwayzeConfig
import st.emily.swayze.types.NetworkConfiguration


/**
 * This test class is going to go away, but I'm committing it for the
 * time being.
 */
class ConfigurationSpec extends Spec {
  class SwayzeConfig {
    @Test def `loads networks` = {
      val config = SwayzeConfig.getConfig
      val networkConfigs = config.getConfigList("swayze.networks").map { network =>
        NetworkConfiguration(name     = network.getString("name"),
                             host     = network.getString("host"),
                             port     = network.getInt("port"),
                             channels = network.getStringList("channels").toList,
                             modules  = network.getStringList("modules").toList)
      }.toList

      networkConfigs.must(be(
        List(NetworkConfiguration(name     = "Ladynet",
                                  host     = "irc.emily.st",
                                  port     = 6667,
                                  channels = List("#parlour"),
                                  modules  = List()))
      ))
    }
  }
}

