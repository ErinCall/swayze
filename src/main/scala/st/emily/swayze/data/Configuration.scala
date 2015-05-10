package st.emily.swayze.data

import com.typesafe.config.Config


case class SwayzeConfig(config: Config) {
  import scala.collection.JavaConversions._

  def getNetworkConfigs: List[NetworkConfig] = {
    config.getConfigList("swayze.networks").map { network =>
      NetworkConfig(network.getString("name"),
                    network.getString("host"),
                    network.getInt("port"),
                    network.getString("encoding"),
                    network.getStringList("channels").toList,
                    network.getStringList("modules").toList,
                    network.getString("nickname"))
    }.toList
  }
}

/**
 * Represents a single network's configuration
 */
case class NetworkConfig(name:     String,
                         host:     String,
                         port:     Int,
                         encoding: String,
                         channels: Seq[String],
                         modules:  Seq[String],
                         nickname: String) {

  /**
   * Get a version of the network name safe to use in the name of the
   * actors which interact with it.
   *
   * TODO: Handle situations where this name conflicts with another network
   * after conversion.
   */
  def uriSafeName: String = {
    val pipeline = Seq({ s: String => s.toLowerCase                   },
                       { s: String => s.replaceAll("[^0-9a-z]+", "-") })

    pipeline.foldLeft(name) { case (s, f) => f(s) }
  }
}
