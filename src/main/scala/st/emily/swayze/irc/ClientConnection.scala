package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.{ SSLContext, SSLEngine, TrustManager, X509TrustManager }

import st.emily.swayze.representation.NetworkConfiguration


object ClientConnection {
  def props(remote:  InetSocketAddress,
            service: ActorRef,
            config:  NetworkConfiguration) =
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
class ClientConnection(remote:  InetSocketAddress,
                       service: ActorRef,
                       config:  NetworkConfiguration) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  private[this] var leftover: String = ""

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  private[this] var transferred: Long = 0

  override def postStop: Unit = log.info(s"Transferred $transferred bytes from/to [$remote]")

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) =>
      sender() ! Register(self, keepOpenOnPeerClosed = true)

    case Received(data) =>
      transferred += data.size
      val (lines, last) = partitionMessageLines(leftover + data.decodeString(config.encoding))
      leftover = last.getOrElse("")
      lines.foreach { line => service ! Message(line) }

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
    val data = ByteString(text + "\r\n", config.encoding)
    transferred += data.size
    sender() ! Write(data)
  }

  def send(message: Message): Unit = {
    val data = ByteString(message.toString + "\r\n", config.encoding)
    transferred += data.size
    sender() ! Write(data)
  }

  /**
   * Last line is defined iff there's a partial line at the end. This
   * then gets stored in the calling method and passed back in when the
   * rest of the line arrives.
   */
  private[this] def partitionMessageLines(text: String): (Array[String], Option[String]) = {
    val (lines, last) = text.linesWithSeparators.toArray.partition(_.endsWith("\r\n"))
    (lines, last.headOption)
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
