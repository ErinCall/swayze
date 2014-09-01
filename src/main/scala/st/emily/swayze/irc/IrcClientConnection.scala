package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress


object IrcClientConnection {
  def props(remote: InetSocketAddress, service: ActorRef) =
    Props(classOf[IrcClientConnection], remote, service)
}

class IrcClientConnection(remote: InetSocketAddress, service: ActorRef) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  override def receive: Receive = {
    case Connected(remote, local) =>
      log.debug("Remote address {} connected", remote)

      sender() ! Register(self)
      context.watch(self) // XXX does this set up death watch correctly?

      context.become {
        case Received(data) =>
          val text = data.utf8String.trim
          log.debug("Received '{}' from remote address {}", text, remote)

          // XXX buffer up, build IrcMessage objects, send to service actor

          service ! text

        case _: ConnectionClosed =>
          log.debug("Stopping, because connection for remote address {} closed", remote)
          context.stop(self)

        case Terminated(`self`) =>
          log.debug("Stopping, because connection for remote address {} died", remote)
          context.stop(self)
      }
  }
}

