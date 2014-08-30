package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging }
import akka.util.ByteString


class Client extends Actor with ActorLogging {
  def receive = {
    case data: ByteString =>
      log.info("got: %s".format(data.decodeString("utf-8")))
    case message: String =>
      log.info("got: %s".format(message))
  }
}
