package st.emily.swayze.conf

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

import st.emily.swayze.representation.NetworkConfiguration


object SwayzeConfig {
  def apply(text: String): SwayzeConfig = {
    new SwayzeConfig(text)
  }
}


class SwayzeConfig(text: String) {
  final val config = ConfigFactory.parseString(text)

  def getNetworkConfigs: List[NetworkConfiguration] = {
    config.getConfigList("swayze.networks").map { network =>
      NetworkConfiguration(name     = network.getString("name"),
                           host     = network.getString("host"),
                           port     = network.getInt("port"),
                           encoding = network.getString("encoding"),
                           channels = network.getStringList("channels").toList,
                           modules  = network.getStringList("modules").toList)
    }.toList
  }
}
