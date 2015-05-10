package st.emily.swayze

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.LoggingReceive
import com.typesafe.config.Config
import java.net.InetSocketAddress

import st.emily.swayze.data.SwayzeConfig
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
  config.getNetworkConfigs.foreach { networkConfig =>
    val name    = networkConfig.uriSafeName
    val remote  = new InetSocketAddress(networkConfig.host, networkConfig.port)
    val service = system.actorOf(ClientService.props(networkConfig), name + "-client-service")
    system.actorOf(ClientConnection.props(remote, service, networkConfig.encoding), name + "-client-connection")
  }

  override def receive: Receive = LoggingReceive {
    case _ =>
  }
}
