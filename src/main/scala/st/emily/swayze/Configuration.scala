package st.emily.swayze

import com.typesafe.config.{ ConfigFactory, ConfigParseOptions, ConfigSyntax }


object SwayzeConfig {
  final val tempConfig =
    """
    swayze {
      networks = [
        {
          name     = Ladynet
          host     = irc.emily.st
          port     = 6667
          channels = [ "#parlour" ]
          modules  = []
        }
      ]
    }
    """

  def getConfig() = ConfigFactory.parseString(
    tempConfig,
    ConfigParseOptions
      .defaults()
      .setSyntax(ConfigSyntax.CONF)
  )
}

