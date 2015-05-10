package st.emily.swayze

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments

import st.emily.swayze.data.{ NetworkConfig, SwayzeConfig }


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

    val swayzeConfig   = ConfigFactory.parseString(configText)
    val appConfig      = ConfigFactory.load
    val finalConfig    = swayzeConfig.withFallback(appConfig)

    val system         = ActorSystem("bouncer-system", finalConfig)
    val bouncerService = BouncerService.props(system, SwayzeConfig(finalConfig))
    val bouncerActor   = system.actorOf(bouncerService, "bouncer-service")
  } catch {
    case e: Throwable => println(s"Couldn't start due to error: ${e.getMessage}")
  }
}
