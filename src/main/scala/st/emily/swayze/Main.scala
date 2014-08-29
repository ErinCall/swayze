package st.emily.swayze

import akka.actor.{ ActorSystem, Props }
import java.net.InetSocketAddress
import st.emily.swayze.irc.{ Client, ClientListener }


object SwayzeApp {
  def main(args: Array[String]) {
    val system   = ActorSystem("tcp")
    val listener = system.actorOf(Props[ClientListener], name = "listener")
    val client   = system.actorOf(
      Props(new Client(new InetSocketAddress("irc.emily.st", 6667), listener)),
      name = "client"
    )
  }
}
