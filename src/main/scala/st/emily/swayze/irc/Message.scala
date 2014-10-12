package st.emily.swayze.irc


abstract sealed class Message(prefix:     Option[String],
                              command:    String,
                              parameters: Seq[String]) // up to 15 params

case class Privmsg(prefix:     Option[String],
                   command:    String,
                   parameters: Seq[String],
                   action:     Boolean,
                   target:     String) extends Message(prefix, command, parameters)

case class Numerical(prefix:     Option[String],
                     command:    String,
                     parameters: Seq[String],
                     number:     Short) extends Message(prefix, command, parameters)


object Message extends MessageParser {
  def apply(line: String): Message = {
    val (prefix, command, parameters) = parse(line)

    command.toUpperCase match {
      case "PRIVMSG" => Privmsg(prefix, command, parameters, false, "")
    }
  }
}


trait MessageParser {
  def parse(text: String): (Option[String], String, Seq[String]) = {
    (Option(""), "", Seq())
  }
}
