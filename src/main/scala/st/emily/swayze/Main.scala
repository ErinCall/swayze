package st.emily.swayze

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import scala.collection.JavaConversions._

import st.emily.swayze.representation.NetworkConfiguration


/**
 * Application entry point
 */
object SwayzeApp extends App {
  val parser = ArgumentParsers.newArgumentParser("swayze")
  parser
    .addArgument("configuration")
    .metavar("swayze.conf")
    .nargs(1)
    .help("The filename containing Swayze's configuration")
    .`type`(Arguments.fileType.verifyIsFile.verifyCanRead.verifyCanWrite)

  try {
    val res            = parser.parseArgsOrFail(args)
    val configFile     = res.getList[java.io.File]("configuration").get(0)
    val configText     = io.Source.fromFile(configFile).mkString

    val system         = ActorSystem("bouncer-system", ConfigFactory.parseString(configText))
    val bouncerService = BouncerService.props(system, SwayzeConfig(configText))
    val bouncerActor   = system.actorOf(bouncerService, "bouncer-service")
  } catch {
    case e: Exception =>
      println(s"Couldn't start due to error: ${e.getMessage}")
  }
}

case class SwayzeConfig(text: String) {
  lazy val config = ConfigFactory.parseString(text)

  def getNetworkConfigs: List[NetworkConfiguration] = {
    config.getConfigList("swayze.networks").map { network =>
      NetworkConfiguration(name     = network.getString("name"),
                           host     = network.getString("host"),
                           port     = network.getInt("port"),
                           encoding = network.getString("encoding"),
                           channels = network.getStringList("channels").toList,
                           modules  = network.getStringList("modules").toList,
                           nickname = network.getString("nickname"))
    }.toList
  }
}
