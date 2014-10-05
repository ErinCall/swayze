package st.emily.swayze

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import com.typesafe.config.Config
import java.net.InetSocketAddress

import st.emily.swayze.conf.SwayzeConfig
import st.emily.swayze.irc.{ ClientConnection, ClientService }


object BouncerService {
  def props(system: ActorSystem, config: SwayzeConfig) =
    Props(classOf[BouncerService], system, config)
}


/**
 * Top-level supervisor responsible for managing client and server
 * services.
 *
 * @param config Swayze configuration
 */
class BouncerService(system: ActorSystem, config: SwayzeConfig) extends Actor with ActorLogging {
  val remote  = new InetSocketAddress("irc.emily.st", 6667)
  val service = system.actorOf(ClientService.props(), "client-service")

  system.actorOf(ClientConnection.props(remote, service), "client-connection")

  override def receive: Receive = {
    case _ =>
  }
}
