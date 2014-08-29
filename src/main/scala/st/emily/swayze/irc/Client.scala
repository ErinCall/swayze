package st.emily.swayze.irc

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import grizzled.slf4j.Logging
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.{ SSLContext, SSLEngine, TrustManager, X509TrustManager }


object Client {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Client], remote, replies)
}

class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  final val sslEngine: SSLEngine = {
    val context = SSLContext.getInstance("TLSv1.1")

    val trustManagers: Array[TrustManager] = Array(new X509TrustManager {
      def checkClientTrusted(arg0: Array[X509Certificate], arg1: String): Unit = ()
      def checkServerTrusted(arg0: Array[X509Certificate], arg1: String): Unit = ()
      def getAcceptedIssuers(): Array[X509Certificate] = Array()
    })

    context.init(null, trustManagers, null)
    val engine = context.createSSLEngine(remote.getHostName, remote.getPort)
    engine.setUseClientMode(true)
    engine
  }

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          listener ! "write failed"  // O/S buffer was full
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}

