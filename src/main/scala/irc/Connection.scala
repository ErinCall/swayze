package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.{ SSLContext, SSLEngine, TrustManager, X509TrustManager }

import st.emily.swayze.data.{ FailedParseException, NetworkConfig }
import st.emily.swayze.irc.{ Message => IrcMessage }


object Connection {
  sealed trait Event
  case object Connected extends Event
  case object Disconnected extends Event

  def props(remote:  InetSocketAddress,
            service: ActorRef) = Props(classOf[Connection], remote, service)
}

/**
 * Maintains the network connection to an IRC server.
 *
 * Provides the TCP connection to an IRC server on behalf of its daemon
 * supervisor. Delegates IRC events to a client service for handling.
 *
 * @param remote the server's info for connecting (a host and port)
 * @param service client service for handling IRC events
 * @param encoding The byte encoding to use to translate bytes to and from strings
 */
class Connection(remote:  InetSocketAddress,
                 service: ActorRef) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  @volatile private[this] var leftover:      String   = ""
  @volatile private[this] var connection:    ActorRef = null

  @volatile private[this] var bytesReceived: Long     = 0
  @volatile private[this] var bytesSent:     Long     = 0
  @volatile private[this] var readsReceived: Long     = 0
  @volatile private[this] var writesSent:    Long     = 0
  @volatile private[this] var writesAcked:   Long     = 0

  override def postStop: Unit = {
    log.debug(f"Sent ${bytesSent} bytes to [${remote}]")
    log.debug(f"Received ${bytesReceived} bytes from [${remote}]")
    log.debug(f"Handled ${readsReceived} receive events")
    log.debug(f"Sent ${writesSent} write events")
    log.debug(f"Received ${writesAcked} write acknowledgements")
  }

  case class Ack(id: Long) extends Event

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) => {
      connection = sender
      connection ! Register(self)
      service ! Connection.Connected
    }

    case Received(data) => {
      readsReceived += 1
      bytesReceived += data.size

      val (lines, last) = partitionMessageLines(leftover + data.utf8String)
      leftover = last.getOrElse("")
      lines.foreach { line =>
        try {
          service ! IrcMessage(line)
        } catch {
          case e: FailedParseException => {
            log.warning(e.getMessage)
          }
        }
      }
    }

    case message: IrcMessage => { // outgoing
      send(message)
    }

    case message: String => {     // outgoing
      send(message)
    }

    case Ack(id) => {
      writesAcked += 1
    }

    case PeerClosed => {
      connection ! Close
      service ! Connection.Disconnected
      context.stop(self)
    }

    case ErrorClosed =>

    case CommandFailed(_: Write) => {
      connection ! ResumeWriting
    }

    case Terminated(self) => {
      log.error("Connection lost")
      context.stop(self)
    }
  }

  private[this] def send(text: String): Unit = {
    val data = ByteString(text + "\r\n")
    bytesSent += data.size
    writesSent += 1
    connection ! Write(data, Ack(writesSent))
  }

  private[this] def send(message: IrcMessage): Unit = {
    val data = ByteString(message.toString)
    bytesSent += data.size
    writesSent += 1
    connection ! Write(data, Ack(writesSent))
  }

  /**
   * Last line is defined iff there's a partial line at the end. This
   * then gets stored in the calling method and passed back in when the
   * rest of the line arrives.
   *
   * Using raw Unicode characters here to ensure I split in an encoding-
   * agnostic way.
   */
  private[this] def partitionMessageLines(text: String): (Array[String], Option[String]) = {
    val (lines, last) = text.split("(?<=" + IrcMessage.crlf + ")")
                            .span(_.endsWith(IrcMessage.crlf))
    (lines.map(_.trim), last.headOption)
  }
}

object SslEngineBuilder {
  def getTrustingEngine(host: String, port: Int): SSLEngine = {
    val tlsContext = SSLContext.getInstance("TLSv1.1")

    val trustManagers: Array[TrustManager] = Array(new X509TrustManager {
      def checkClientTrusted(arg0: Array[X509Certificate], arg1: String): Unit = ()
      def checkServerTrusted(arg0: Array[X509Certificate], arg1: String): Unit = ()
      def getAcceptedIssuers(): Array[X509Certificate] = Array()
    })

    tlsContext.init(null, trustManagers, null)
    val engine = tlsContext.createSSLEngine(host, port)
    engine.setUseClientMode(true)
    engine
  }
}
