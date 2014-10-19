package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
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
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  override def postRestart(thr: Throwable): Unit = context.stop(self)

  private[this] var messagesReceived: Long = 0
  private[this] var messagesSent:     Long = 0

  override def postStop: Unit = {
    log.info(s"Sent $messagesSent messages")
    log.info(s"Received $messagesReceived messages")
  }

  override def receive: Receive = {
    case Ready =>
      sender() ! Message(Command.NICK, config.nickname)
      sender() ! Message(Command.USER, config.nickname, config.nickname, "*", config.nickname)

    case message: Message if message.command == Option(Command.PING) =>
      messagesReceived += 1
      sender() ! Message(command    = Option(Command.PONG),
                         parameters = Seq(message.pingValue.getOrElse("")))
      messagesSent += 1

    case message: Message if message.numeric.isDefined &&
                             message.numeric == Option(Numeric.RPL_WELCOME) =>
      messagesReceived += 1
      config.channels.foreach { channel => 
        sender() ! Message(Command.JOIN, channel)
        messagesSent += 1
      }

    case _ =>
  }
}
