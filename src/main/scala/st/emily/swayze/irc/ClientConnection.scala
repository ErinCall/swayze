package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress

import st.emily.swayze.representation.NetworkConfiguration


object ClientConnection {
  def props(remote: InetSocketAddress,
            service: ActorRef,
            config: NetworkConfiguration) =
    Props(classOf[ClientConnection], remote, service, config)
}


/**
 * Maintains the network connection to an IRC server.
 *
 * Provides the TCP connection to an IRC server on behalf of its daemon
 * supervisor. Delegates IRC events to a client service for handling.
 *
 * @param remote the server's info for connecting (a host and port)
 * @param service client service for handling IRC events
 * @param config The configuration specific to this network
 */
class ClientConnection(remote: InetSocketAddress,
                       service: ActorRef,
                       config: NetworkConfiguration) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) =>
      sender() ! Register(self, keepOpenOnPeerClosed = true)

      send("NICK swayze")
      send("USER swayze swayze localhost :swayze")

    case Received(data) =>
      transferred += data.size

      if (data.decodeString(config.encoding).trim.startsWith("PING")) {
        send(data.decodeString(config.encoding).trim.replaceAll("PING", "PONG"))
        send("JOIN #swayze")
      }

    case PeerClosed =>
      sender() ! Close
      context.stop(self)

    case CommandFailed(_: Write) =>
      context.stop(self)

    case Terminated(self) =>
      log.error("Connection lost")
      context.stop(self)
  }

  def send(text: String): Unit = {
    sender() ! Write(ByteString(text + "\r\n", config.encoding))
  }

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  var transferred: Long = 0
  override def postStop: Unit = log.info(s"Transferred $transferred bytes from/to [$remote]")
}
