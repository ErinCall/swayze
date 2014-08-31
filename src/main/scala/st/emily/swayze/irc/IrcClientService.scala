package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import akka.io.Tcp
import java.net.InetSocketAddress


object IrcClientService {
  def props(remote: InetSocketAddress, connection: ActorRef): Props =
    Props(new IrcClientService(remote, connection))
}


class IrcClientService(remote: InetSocketAddress, connection: ActorRef) extends Actor with ActorLogging {
  context.watch(connection)

  def receive: Receive = {
    case Tcp.Received(data) =>
      val text = data.utf8String.trim
      log.debug("Received '{}' from remote address {}", text, remote)

      println(text)
      // text match {
      //   case "close" => context.stop(self)
      //   case _       => sender ! Tcp.Write(data)
      // }

    case _: Tcp.ConnectionClosed =>
      log.debug("Stopping, because connection for remote address {} closed", remote)
      context.stop(self)

    case Terminated(`connection`) =>
      log.debug("Stopping, because connection for remote address {} died", remote)
      context.stop(self)
  }
}

