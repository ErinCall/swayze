package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import java.net.InetSocketAddress
import st.emily.swayze.irc.{ ClientConnection, ClientService }


/** Application entry point */
object SwayzeApp extends App {
  val system = ActorSystem("client-service-system")
  val remote = new InetSocketAddress("irc.emily.st", 6667)
  val service = system.actorOf(ClientService.props(), "client-service")
  system.actorOf(ClientConnection.props(remote, service), "client-connection")
}
