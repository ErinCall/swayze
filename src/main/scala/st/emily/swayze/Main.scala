package st.emily.swayze

import akka.actor.{ ActorSystem, Props }

import st.emily.swayze.conf.SwayzeConfig


/**
 * Application entry point
 */
object SwayzeApp extends App {
  val tempConfig = """
                   swayze {
                     networks = [
                       {
                         name     = Ladynet
                         host     = irc.emily.st
                         port     = 6667
                         encoding = UTF-8
                         channels = [ "#swayze" ]
                         modules  = []
                         nickname = "swayze"
                       }
                       {
                         name     = Freenode
                         host     = irc.freenode.net
                         port     = 6667
                         encoding = UTF-8
                         channels = [ "#swayze" ]
                         modules  = []
                         nickname = "swayze"
                       }
                     ]
                   }
                   """

  val system  = ActorSystem("bouncer-system")
  val bouncer = system.actorOf(
    BouncerService.props(system, SwayzeConfig(tempConfig)),
    "bouncer-service"
  )
}
