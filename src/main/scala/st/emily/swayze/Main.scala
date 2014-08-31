package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import java.net.InetSocketAddress
import st.emily.swayze.irc.{ IrcClientConnection }


object SwayzeApp {
  def main(args: Array[String]) {
    val system = ActorSystem("irc-client-service-system")
    val remote = new InetSocketAddress("irc.emily.st", 6667)
    system.actorOf(IrcClientConnection.props(remote), "irc-client-service")
  }
}
