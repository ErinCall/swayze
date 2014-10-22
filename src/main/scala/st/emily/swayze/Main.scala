package st.emily.swayze

import akka.actor.ActorSystem
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.internal.HelpScreenException

import st.emily.swayze.conf.SwayzeConfig


/**
 * Application entry point
 */
object SwayzeApp extends App {
  val parser = ArgumentParsers.newArgumentParser("swayze").description("An IRC bouncer")
  parser.addArgument("-c,--configuration")
        .metavar("filename")
        .dest("configFile")
        .help("the filename with the configuration file")
  try {
    val res     = parser.parseArgs(args)
    val config  = io.Source.fromFile(res.getString("configFile")).mkString
    val system  = ActorSystem("bouncer-system")
    val bouncer = system.actorOf(
      BouncerService.props(system, SwayzeConfig(config)),
      "bouncer-service"
    )
  } catch {
    case hse: HelpScreenException =>
  }
}
