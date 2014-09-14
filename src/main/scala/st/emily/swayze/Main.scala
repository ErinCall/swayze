package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.ConfigFactory


/**
 * Application entry point
 */
object SwayzeApp extends App {
  val system  = ActorSystem("bouncer-system")

  val bouncer = system.actorOf(
    BouncerService.props(system, ConfigFactory.load()),
    "bouncer-service"
  )
}

