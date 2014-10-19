package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.representation.NetworkConfiguration


case object Ready

object ClientService {
  def props(config: NetworkConfiguration): Props = Props(new ClientService(config))
}

/**
 * Handles IRC events.
 *
 * @param config The configuration specific to this network
 */
class ClientService(config: NetworkConfiguration) extends Actor with ActorLogging {
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  override def postRestart(thr: Throwable): Unit = context.stop(self)

  override def receive: Receive = {
    case Ready =>
      sender() ! Message(Command.NICK, config.nickname)
      sender() ! Message(Command.USER, config.nickname, config.nickname, "*", config.nickname)

    case message: Message if message.command == Option(Command.PING) =>
      sender() ! Message(Command.PONG, message.pingValue.getOrElse(config.host))

    case message: Message if message.numeric == Option(Numeric.RPL_WELCOME) =>
      config.channels.foreach(sender() ! Message(Command.JOIN, _))

    case _ =>
  }
}
