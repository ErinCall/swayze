package st.emily.swayze.representation


/**
 * Represents a single network's configuration
 */
case class NetworkConfiguration(name:     String,
                                host:     String,
                                port:     Int,
                                encoding: String,
                                channels: Seq[String],
                                modules:  Seq[String]) {

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
