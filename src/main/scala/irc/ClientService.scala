package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.data.NetworkConfig
import Command._
import Numeric._


sealed trait MetaMessage
case object Ready extends MetaMessage
case object LoggedIn extends MetaMessage

object ClientService {
  def props(config: NetworkConfig): Props = Props(new ClientService(config))
}

/**
 * Handles IRC events.
 *
 * @param config The configuration specific to this network
 */
class ClientService(config: NetworkConfig) extends Actor with ActorLogging {
  override final val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  override def postRestart(thr: Throwable): Unit = context.stop(self)

  override def receive: Receive = {
    case Ready =>
      log.debug("Connected, sending login...")
      sender ! Message(NICK, config.nickname)
      sender ! Message(USER, config.nickname, config.nickname, "*", config.nickname)

    case LoggedIn =>
      log.debug(f"Logged in, joining ${config.channels.mkString(",")}...")
      sender ! Message(JOIN, config.channels.mkString(","))

    case message: Message =>
      (message.command, message.numeric) match {
        case (None, Some(RPL_WELCOME)) =>
          self ! LoggedIn

        case (Some(PING), _) =>
          log.debug(f"Got PING! Replying PONG with ${message.parameters(0)}...")
          sender ! Message(PONG, message.parameters(0))

        case _ =>

      }
  }
}
