package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.representation.NetworkConfiguration


object ClientService {
  def props(config: NetworkConfiguration): Props =
    Props(new ClientService(config))
}

/** Handles IRC events.  */
class ClientService(config: NetworkConfiguration) extends Actor with ActorLogging {
  // XXX handle IrcMessage objects
  // XXX receive connection for sending messages back
  def receive: Receive = {
    case message: String =>
    case _ =>
  }
}
