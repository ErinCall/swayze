package st.emily.swayze.irc


abstract sealed class Message

case class Privmsg() extends Message

object Message {
  def apply(line: String): Message = {
    Privmsg()
  }
}
