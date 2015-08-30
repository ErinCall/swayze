package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.Tcp
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.util.matching.Regex

import st.emily.swayze.data.NetworkConfig
import Command._
import Numeric._


sealed trait ClientEvent
case object ClientReady extends ClientEvent
case object ClientLoggedIn extends ClientEvent

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
  override def postRestart(t: Throwable): Unit = context.stop(self)

  override def receive: Receive = LoggingReceive {
    case ClientReady => {
      log.debug("Connected, sending login...")
      sender ! Message(NICK, config.nickname)
      sender ! Message(USER, config.nickname, config.nickname, "*", config.nickname)
    }

    case ClientLoggedIn => {
      log.debug(f"Logged in, joining ${config.channels.mkString(",")}...")
      sender ! Message(JOIN, config.channels.mkString(","))
    }

    case message: Message => {
      (message.command, message.numeric) match {
        case (_, Some(RPL_WELCOME)) => {
          log.debug(message.toString)
          self ! ClientLoggedIn
        }

        case (_, Some(RPL_WHOREPLY)) => {
          log.debug(message.toString)
        }

        case (Some(PING), _) => {
          val pongValue = message.parameters(0)
          log.debug(f"Got PING! Replying PONG with '${pongValue}'...")
          sender ! Message(PONG, pongValue)
        }

        case _ => {
          log.debug(message.toString)
        }
      }
    }
  }
}
