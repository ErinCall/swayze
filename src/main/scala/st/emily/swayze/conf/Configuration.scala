package st.emily.swayze.conf

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

import st.emily.swayze.types.NetworkConfiguration


object SwayzeConfig {
  def apply(config: String): SwayzeConfig = {
    new SwayzeConfig(config)
  }
}


class SwayzeConfig(text: String) {
  final val config = ConfigFactory.parseString(text)

  def getNetworkConfigs: List[NetworkConfiguration] = {
    config.getConfigList("swayze.networks").map { network =>
      NetworkConfiguration(name     = network.getString("name"),
                           host     = network.getString("host"),
                           port     = network.getInt("port"),
                           channels = network.getStringList("channels").toList,
                           modules  = network.getStringList("modules").toList)
    }.toList
  }
}

