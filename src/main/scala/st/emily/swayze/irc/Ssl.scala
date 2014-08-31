package st.emily.swayze.irc

import java.security.cert.X509Certificate
import javax.net.ssl.{ SSLContext, SSLEngine, TrustManager, X509TrustManager }


object SslEngineBuilder {
  def getSslEngine(host: String, port: Int): SSLEngine = {
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
