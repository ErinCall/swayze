package st.emily.swayze.irc

import akka.actor.Actor
import akka.event.Logging
import akka.util.ByteString


class ClientListener extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case data: ByteString => log.info("got: %s".format(data.decodeString("utf-8")))
    case message: String => log.info("got: %s".format(message))
  }
}
