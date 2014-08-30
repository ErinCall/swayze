package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import java.net.InetSocketAddress
import st.emily.swayze.irc.{ Client, Connection }


object SwayzeApp {
  def main(args: Array[String]) {
    val system   = ActorSystem("tcp")
    val client = system.actorOf(Props[Client], name = "client")
    val connection   = system.actorOf(
      Props(new Connection(new InetSocketAddress("irc.emily.st", 6667), client)),
      name = "connection"
    )
  }
}
