package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric


abstract sealed class Message(val prefix:     Option[String],
                              val command:    Command,
                              val parameters: Seq[String]) // up to 15 params

case class Privmsg(override val prefix:     Option[String],
                   override val command:    Command,
                   override val parameters: Seq[String],
                   action:                  Boolean,
                   target:                  String) extends Message(prefix, command, parameters)

case class Reply(override val prefix:     Option[String],
                 override val command:    Command,
                 override val parameters: Seq[String],
                 numeric:                 Numeric) extends Message(prefix, command, parameters)


object Message extends MessageParser {
  def apply(line: String): Message = {
    val (prefix, command, parameters) = parse(line)

    command match {
      case Command.PRIVMSG => Privmsg(prefix, command, parameters, false, "")
    }
  }
}


trait MessageParser {
  def parse(text: String): (Option[String], Command, Seq[String]) = {
    (Option(""), Command.PRIVMSG, Seq())
  }
}

