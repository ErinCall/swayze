package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.representation.NetworkConfiguration
import Command.Command
import Numeric.Numeric


case object Ready

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
    case Ready =>
      sender() ! Message(Command.NICK, config.nickname)
      sender() ! Message(Command.USER, config.nickname, config.nickname, "*", config.nickname)

    case message: Message if message.command == Option(Command.PING) =>
      log.debug(s"got $message")
      sender() ! Message(command    = Option(Command.PONG),
                         parameters = Seq(message.pingValue.getOrElse("")))

    case message: Message if message.numeric.isDefined &&
                             message.numeric == Option(Numeric.RPL_WELCOME) =>
      config.channels.foreach(sender() ! Message(Command.JOIN, _))

    case _ =>
  }
}
