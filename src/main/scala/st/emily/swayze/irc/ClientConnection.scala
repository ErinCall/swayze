package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.event.LoggingReceive
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.{ SSLContext, SSLEngine, TrustManager, X509TrustManager }

import st.emily.swayze.representation.NetworkConfiguration
import st.emily.swayze.irc.{ Message => IrcMessage }


object ClientConnection {
  def props(remote:   InetSocketAddress,
            service:  ActorRef,
            encoding: String) =
    Props(classOf[ClientConnection], remote, service, encoding)
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
class ClientConnection(remote:   InetSocketAddress,
                       service:  ActorRef,
                       encoding: String) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  private[this] var leftover:      String   = ""
  private[this] var connection:    ActorRef = null

  private[this] var transferred:   Long     = 0
  private[this] var readsReceived: Long     = 0
  private[this] var writesSent:    Long     = 0
  private[this] var writesAcked:   Long     = 0

  override def postStop: Unit = {
    log.info(s"Transferred $transferred bytes from/to [$remote]")
    log.info(s"Handled $readsReceived receive events")
    log.info(s"Sent $writesSent write events")
    log.info(s"Received $writesAcked write acknowledgements")
  }

  case class Ack(id: Long) extends Event

  override def receive: Receive = LoggingReceive {
    case Connected(remote, local) =>
      connection = sender()
      connection ! Register(self)
      service ! Ready

    case Received(data) =>
      readsReceived += 1
      transferred += data.size

      val (lines, last) = partitionMessageLines(leftover + data.decodeString(encoding))
      leftover = last.getOrElse("")
      lines.foreach { line => service ! IrcMessage(line) }

    case message: IrcMessage =>
      send(message)

    case message: String =>
      send(message)

    case Ack(id) =>
      writesAcked += 1

    case PeerClosed =>
      connection ! Close
      context.stop(self)

    case ErrorClosed =>

    case CommandFailed(_: Write) =>
      connection ! ResumeWriting

    case Terminated(self) =>
      log.error("Connection lost")
      context.stop(self)
  }

  def send(text: String): Unit = {
    val data = ByteString(text + "\r\n", encoding)
    transferred += data.size
    writesSent += 1
    connection ! Write(data, Ack(writesSent))
  }

  def send(message: IrcMessage): Unit = {
    val data = ByteString(message.toRawMessageString, encoding)
    transferred += data.size
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
    val (lines, last) = text.split("\u000D\u000A").partition(!_.endsWith("\u000D\u000A"))
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
