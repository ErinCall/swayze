package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex


object ClientService {
  def props(): Props =
    Props(new ClientService())
}

/** Handles IRC events.  */
class ClientService() extends Actor with ActorLogging {
  // XXX handle IrcMessage objects
  // XXX receive connection for sending messages back
  def receive: Receive = {
    case message: String =>
      log.info("got message {}", message)
    case _ =>
      log.info("got unknown")
  }
}

