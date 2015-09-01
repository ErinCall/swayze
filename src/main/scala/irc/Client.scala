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


object Client {
  sealed trait Event
  case object LoggedIn extends Event

  def props(config: NetworkConfig) = Props(new Client(config))
}

/**
 * Handles IRC events.
 *
 * @param config The configuration specific to this network
 */
class Client(config: NetworkConfig) extends Actor with ActorLogging {
  override final val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  override def postRestart(t: Throwable): Unit = context.stop(self)

  override def receive: Receive = LoggingReceive {
    case Connection.Connected => {
      log.debug("Connected, sending login...")
      sender ! Message(NICK, config.nickname)
      sender ! Message(USER, config.nickname, config.nickname, "*", config.nickname)
    }

    case Client.LoggedIn => {
      log.debug(f"Logged in, joining ${config.channels.mkString(",")}...")
      sender ! Message(JOIN, config.channels.mkString(","))
    }

    case message: Message => {
      (message.command, message.numeric) match {
        case (_, Some(RPL_WELCOME)) => {
          log.debug(message.toString)
          self ! Client.LoggedIn
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
