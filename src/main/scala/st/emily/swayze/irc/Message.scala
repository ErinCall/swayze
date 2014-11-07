package st.emily.swayze.irc

import scala.util.{ Failure, Success, Try }

import st.emily.swayze.exceptions.FailedParseException
import Command.Command
import Numeric.Numeric


abstract class Message(val raw:        Option[String]  = None,
                       val prefix:     Option[String]  = None,
                       val command:    Option[Command] = None,
                       val numeric:    Option[Numeric] = None,
                       val parameters: Seq[String]     = Seq()) {
  lazy val toRawMessage = raw.getOrElse {
    val rawMessage = new scala.collection.mutable.StringBuilder(510) // TODO: enforce this limit
    if (prefix.isDefined) rawMessage.append(prefix.get + "\u0020")
    rawMessage.append(command.getOrElse(numeric.get))

    // last parameter is the "trailing" one and starts with a colon
    parameters.zipWithIndex.foreach { case (parameter, i) =>
      val trailing = if (i == parameters.length - 1) ":" else ""
      rawMessage.append("\u0020" + trailing + parameter)
    }

    rawMessage.toString + "\r\n"
  }
}

object Message {
  def apply(line: String): Message = {
    try {
      val (prefix, command, numeric, parameters) = fromRawMessage(line)
      command.getOrElse(Command.REPLY) match {
        case Command.REPLY   => Reply(Option(line), prefix, numeric, parameters)

        case Command.PRIVMSG => Privmsg(Option(line), prefix, parameters)
        case Command.PING    => Ping(Option(line), prefix, parameters)
        case Command.PONG    => Pong(Option(line), prefix, parameters)
        case Command.MODE    => Mode(Option(line), prefix, parameters)
        case Command.JOIN    => Join(Option(line), prefix, parameters)
        case Command.NICK    => Nick(Option(line), prefix, parameters)
      }
    } catch {
      case e: Exception => throw FailedParseException(s"Couldn't parse `$line`", e)
    }
  }

  /**
   * Breaks a raw IRC message string into parts ready for a Message
   * constructor.
   *
   * @param text Raw IRC message string, optionally with linebreak
   *   remaining at the end.
   *
   * @return A four-tuple with an optional prefix as a string, a
   *   command, an optional numeric code as a string received in the
   *   message, and a sequence of parameters as strings.
   *
   * @see http://tools.ietf.org/html/rfc2812#section-2.3.1
   */
  def fromRawMessage(text: String): (Option[String], Option[Command], Option[Numeric], Seq[String]) = {
    val tokens = text.split("\u0020").map(_.trim)

    val prefix =
      tokens(0)(0) match {
        case ':' => Option(tokens(0))
        case _ => None
      }

    val commandOrReply = tokens(if (prefix.isDefined) 1 else 0).toUpperCase
    val command =
      Try(Command.withName(commandOrReply)) match {
        case Success(command) => Option(command)
        case Failure(_) => None
      }
    val numeric =
      if (!command.isDefined) {
        Try(Numeric.withName(commandOrReply)) match {
          case Success(numeric) => Option(numeric)
          case Failure(_) => Option(Numeric.UNKNOWN)
        }
      } else None

    val params = tokens.drop(if (prefix.isDefined) 2 else 1).takeWhile(!_.startsWith(":"))
    val trailing = text.split(":", 3).lastOption.map(_.stripLineEnd)

    (prefix, command, numeric, params ++ trailing)
  }
}

case class Reply(override val raw:        Option[String]  = None,
                 override val prefix:     Option[String]  = None,
                 override val numeric:    Option[Numeric],
                 override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.REPLY), numeric, parameters)

case class Privmsg(override val raw:        Option[String]  = None,
                   override val prefix:     Option[String]  = None,
                   override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.PRIVMSG), None, parameters) {
  require(parameters.size == 2, "A Privmsg must have a target and content")

  lazy val action: Boolean = parameters(1).startsWith("\u0001ACTION")
  lazy val target: String = parameters(0)
  lazy val contents: String =
    if (action) {
      parameters(1).slice(8, parameters(1).length - 1)
    } else {
      parameters(1)
    }
}

case class Ping(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.PING), None, parameters) {
  require(parameters.size == 1, "A Ping must have a value")

  lazy val pingValue: String = parameters(0)
}

case class Pong(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.PONG), None, parameters) {
  require(parameters.size == 1, "A Pong must have a value")

  lazy val pongValue: String = parameters(0)
}

case class Mode(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.MODE), None, parameters) {
  require(parameters.size == 2, "A Mode must have a target and a mode")

  lazy val target: String = parameters(0)
  lazy val mode: String = parameters(1)
}

case class Nick(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.NICK), None, parameters) {
  // require(parameters.size == 1, "A Nick must have a nickname") // TODO handle server nickname commands

  lazy val nickname: String = parameters(0)
}

case class User(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.USER), None, parameters) {
  // require(parameters.size == 1, "A Nick must have a nickname") // TODO handle server nickname commands
}

case class Join(override val raw:        Option[String]  = None,
                override val prefix:     Option[String]  = None,
                override val parameters: Seq[String]     = Seq())
extends Message(raw, prefix, Some(Command.JOIN), None, parameters) {
  // require(parameters.size == 1, "A Nick must have a nickname") // TODO handle server nickname commands
}
