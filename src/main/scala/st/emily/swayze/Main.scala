package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import java.net.InetSocketAddress
import st.emily.swayze.irc.{ ClientConnection, ClientService }


object SwayzeApp extends App {
  val system = ActorSystem("irc-client-service-system")
  val remote = new InetSocketAddress("irc.emily.st", 6667)
  val service = system.actorOf(ClientService.props(), "irc-client-service")
  system.actorOf(ClientConnection.props(remote, service), "irc-client-connection")
}
