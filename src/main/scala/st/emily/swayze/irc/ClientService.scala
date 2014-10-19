package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.representation.NetworkConfiguration
import Command.Command
import Numeric.Numeric


object ClientService {
  def props(config: NetworkConfiguration): Props =
    Props(new ClientService(config))
}

/**
 * Handles IRC events.
 *
 * @param config The configuration specific to this network
 */
class ClientService(config: NetworkConfiguration) extends Actor with ActorLogging {
  override def receive: Receive = {
    case message: Message if message.command == Option(Command.PING) =>
      sender() ! Message(command    = Option(Command.PONG),
                         parameters = Seq(message.pingValue.getOrElse("")))
    case message: String => self ! Message(message)
    case _ =>
  }
}
