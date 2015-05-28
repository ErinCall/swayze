package st.emily.swayze

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.LoggingReceive
import com.typesafe.config.Config
import java.net.InetSocketAddress

import st.emily.swayze.data.SwayzeConfig
import st.emily.swayze.irc.{ ClientConnection, ClientService }



object BouncerService {
  def props(system: ActorSystem, config: SwayzeConfig): Props = {
    Props(classOf[BouncerService], system, config)
  }
}

/**
 * Top-level supervisor responsible for managing client and server
 * services.
 *
 * @param config Swayze configuration
 */
class BouncerService(system: ActorSystem, config: SwayzeConfig) extends Actor with ActorLogging {
  config.getNetConfigs.foreach { netConfig =>
    val name    = netConfig.uriSafeName
    val remote  = new InetSocketAddress(netConfig.host, netConfig.port)
    val service = system.actorOf(ClientService.props(netConfig), name + "-client-service")

    system.actorOf(ClientConnection.props(remote, service, netConfig.encoding), name + "-client-connection")
  }

  override def receive: Receive = LoggingReceive {
    case _ =>
  }
}
