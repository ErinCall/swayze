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

  private[this] var leftover: String = ""

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) =>
      sender() ! Register(self, keepOpenOnPeerClosed = true)

      send("NICK swayze")
      send("USER swayze swayze localhost :swayze")

    case Received(data) =>
      transferred += data.size

      val (lines, extra) =
        splitOffLeftover(leftover + data.decodeString(config.encoding))
      leftover = extra // there is likely more to come

      lines.foreach { line =>
        // XXX all of this will be gone soon
        if (line.trim.startsWith("PING")) {
          send(line.trim.replaceAll("PING", "PONG"))
          send("JOIN #swayze")
        }

        println("=====> " + line)
      }

    case PeerClosed =>
      sender() ! Close
      context.stop(self)

    case ErrorClosed =>

    case CommandFailed(_: Write) =>
      sender() ! ResumeWriting

    case Terminated(self) =>
      log.error("Connection lost")
      context.stop(self)
  }

  def send(text: String): Unit = {
    println("<===== " + text + "\r\n") // XXX fuck off soon
    sender() ! Write(ByteString(text + "\r\n", config.encoding))
  }

  private[this] def splitOffLeftover(text: String): (Array[String], String) = {
    val (lines, leftover) =
      text.linesWithSeparators.toArray.partition(_.endsWith("\r\n"))

    (lines, leftover.headOption.getOrElse(""))
  }

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  var transferred: Long = 0
  override def postStop: Unit = log.info(s"Transferred $transferred bytes from/to [$remote]")
}
