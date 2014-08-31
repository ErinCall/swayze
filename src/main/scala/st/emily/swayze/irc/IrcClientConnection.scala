package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress


object IrcClientConnection {
  def props(remote: InetSocketAddress) =
    Props(classOf[IrcClientConnection], remote)
}

class IrcClientConnection(remote: InetSocketAddress) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  override def receive: Receive = {
    case Tcp.Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)
      sender ! Tcp.Register(context.actorOf(IrcClientService.props(remote, sender)))
  }
}

