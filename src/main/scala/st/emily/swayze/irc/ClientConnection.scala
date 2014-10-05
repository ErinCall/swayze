package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress


object ClientConnection {
  def props(remote: InetSocketAddress, service: ActorRef) =
    Props(classOf[ClientConnection], remote, service)
}


/**
 * Maintains the network connection to an IRC server.
 *
 * Provides the TCP connection to an IRC server on behalf of its daemon
 * supervisor. Delegates IRC events to a client service for handling.
 *
 * @param remote the server's info for connecting (a host and port)
 * @param service client service for handling IRC events
 */
class ClientConnection(remote: InetSocketAddress, service: ActorRef) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) =>
      sender() ! Register(self, keepOpenOnPeerClosed = true)

      sender() ! Write(ByteString("NICK swayze \r\n"))
      sender() ! Write(ByteString("USER swayze swayze localhost :swayze \r\n"))

    case Received(data) =>
      transferred += data.size

      if (data.utf8String.trim.startsWith("PING")) {
        sender() ! Write(ByteString(data.utf8String.replaceAll("PING", "PONG")))
        sender() ! Write(ByteString("JOIN #swayze\r\n"))
      }

    case PeerClosed =>
      context.stop(self)

    case CommandFailed(_: Write) =>
      context.stop(self)

    case Terminated(self) =>
      log.error("Connection lost")
      context.stop(self)
  }

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  var transferred: Long = 0
  override def postStop: Unit = log.info(s"Transferred $transferred bytes from/to [$remote]")

}
