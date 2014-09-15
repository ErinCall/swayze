package st.emily.swayze

import akka.actor.{ ActorSystem, Props }


/**
 * Application entry point
 */
object SwayzeApp extends App {
  val system  = ActorSystem("bouncer-system")

  val bouncer = system.actorOf(
    BouncerService.props(system, SwayzeConfig.getConfig),
    "bouncer-service"
  )
}

