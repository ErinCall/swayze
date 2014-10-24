package st.emily.swayze

import akka.actor.ActorSystem
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments

import st.emily.swayze.conf.SwayzeConfig


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
    val config         = SwayzeConfig(io.Source.fromFile(configFile).mkString)

    val system         = ActorSystem("bouncer-system")
    val bouncerService = BouncerService.props(system, config)
    val bouncerActor   = system.actorOf(bouncerService, "bouncer-service")
  } catch {
    case e: Exception =>
      println(s"Couldn't start due to error: ${e.getMessage}")
  }
}
