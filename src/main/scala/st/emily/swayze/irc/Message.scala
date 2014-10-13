package st.emily.swayze.irc

import scala.util.{ Failure, Success, Try }

import Command.Command


abstract sealed class Message(val raw:        Option[String] = None,
                              val prefix:     Option[String] = None,
                              val command:    Command,
                              val parameters: Seq[String]    = Seq()) {
  override def toString = raw.getOrElse(
    (Seq(prefix.getOrElse(""), command) ++ parameters).mkString(" ") + "\r\n"
  )
}

case class Privmsg(override val raw:        Option[String] = None,
                   override val prefix:     Option[String] = None,
                   override val command:    Command,
                   override val parameters: Seq[String]    = Seq(),
                   action:                  Boolean,
                   target:                  String) extends Message(raw, prefix, command, parameters)

case class Reply(override val raw:        Option[String] = None,
                 override val prefix:     Option[String] = None,
                 override val command:    Command,
                 override val parameters: Seq[String]    = Seq(),
                 numeric:                 String) extends Message(raw, prefix, command, parameters)


object Message extends MessageParser {
  def apply(line: String): Message = {
    val (prefix, command, numeric, parameters) = parse(line)

    command match {
      case Command.PRIVMSG => Privmsg(Option(line), prefix, command, parameters, false, parameters(0))
      case Command.REPLY   => Reply(Option(line), prefix, command, parameters, numeric.get)
    }
  }
}


trait MessageParser {
  /**
   * Breaks a raw IRC message string into parts ready for a Message
   * constructor.
   *
   * @param text Raw IRC message string, optionally with linebreak
   *   remaining at the end.
   *
   * @returns A four-tuple with an optional prefix as a string, a
   *   command, an optional numeric code as a string received in the
   *   message, and a sequence of parameters as strings.
   *
   * @see http://tools.ietf.org/html/rfc2812#section-2.3.1
   */
  def parse(text: String): (Option[String], Command, Option[String], Seq[String]) = {
    val tokens = text.split("\u0020").map(_.trim)

    val prefix = tokens(0)(0) match {
      case ':' => Option(tokens(0))
      case _ => None
    }

    val commandOrReply = tokens(if (prefix.isDefined) 1 else 0).toUpperCase
    val command =
      Try(Command.withName(commandOrReply)) match {
        case Success(command) => command
        case Failure(_)       =>
          Try(commandOrReply.toInt) match {
            case Success(_) => Command.REPLY
            case Failure(_) => Command.UNKNOWN
          }
      }

    val numeric = if (command == Command.REPLY) Option(commandOrReply) else None
    val params = tokens.drop(if (prefix.isDefined) 2 else 1).takeWhile(!_.startsWith(":"))
    val trailing = text.split("\u0020:").lastOption.map(_.trim)

    (prefix, command, numeric, params ++ trailing)
  }
}
