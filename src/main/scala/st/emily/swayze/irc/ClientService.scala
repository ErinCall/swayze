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
      sender() ! Nick(parameters = Seq(config.nickname))
      sender() ! User(parameters = Seq(config.nickname, config.nickname, "*", config.nickname))

    case ping: Ping =>
      sender() ! Pong(parameters = Seq(ping.pingValue))

    case reply: Reply =>
      reply.numeric match {
        case Numeric.RPL_WELCOME =>
          config.channels.foreach { channel: String => sender() ! Join(parameters = Seq(channel)) }

        case _ =>
      }

    case _ =>
  }
}
