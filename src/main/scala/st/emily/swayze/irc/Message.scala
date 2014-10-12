package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric


abstract sealed class Message(val raw:        String,
                              val prefix:     Option[String],
                              val command:    Command,
                              val parameters: Seq[String])

case class Privmsg(override val raw:        String,
                   override val prefix:     Option[String],
                   override val command:    Command,
                   override val parameters: Seq[String],
                   action:                  Boolean,
                   target:                  String) extends Message(raw, prefix, command, parameters)

case class Reply(override val raw:        String,
                 override val prefix:     Option[String],
                 override val command:    Command,
                 override val parameters: Seq[String],
                 numeric:                 Numeric) extends Message(raw, prefix, command, parameters)


object Message extends MessageParser {
  def apply(line: String): Message = {
    val (prefix, command, parameters) = parse(line)

    command match {
      case Command.PRIVMSG => Privmsg(line, prefix, command, parameters, false, parameters(0))
    }
  }
}


/**
 * Parses a message into its parts hewing as close to the intentions of
 * RFC2812 as possible. This includes paying close attention to the
 * actual octets included in the message. This is probably overly
 * cautious if anything; by this point, the string has been decoded
 * using the configured encoding.
 *
 * @see http://tools.ietf.org/html/rfc2812#section-2.3.1
 */
trait MessageParser {
  def parse(text: String): (Option[String], Command, Seq[String]) = {
    val tokens = text.split("\u0020").map(_.trim)

    val prefix = tokens(0)(0) match {
      case ':' => Option(tokens(0))
      case _   => None
    }

    val command =
      try {
        Command.withName(tokens(if (prefix.isDefined) 1 else 0).toUpperCase)
      } catch {
        case e => Command.UNKNOWN
      }

    val params = tokens.drop(if (prefix.isDefined) 2 else 1).takeWhile(!_.startsWith(":"))
    val trailing = text.split("\u0020:").lastOption.map(_.trim)

    (prefix, command, params ++ trailing)
  }
}
