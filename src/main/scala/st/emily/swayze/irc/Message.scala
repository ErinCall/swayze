package st.emily.swayze.irc

import scala.util.{ Failure, Success, Try }

import Command.Command
import Numeric.Numeric


/**
 * This type represents the contents of a single IRC event.
 *
 * I'd like to break this into many types, one for each kind of message,
 * but it's going to be a pain, and I'd like to move on for now. That's
 * why there are a bunch of methods which do specific things for
 * specific kinds of commands. Lots of match statements that I can get
 * rid of eventually. I know it's awful.
 */
case class Message(raw:        Option[String]  = None,
                   prefix:     Option[String]  = None,
                   command:    Option[Command] = None,
                   numeric:    Option[Numeric] = None,
                   parameters: Seq[String]     = Seq()) {
  def toRawMessageString = {
    raw match {
      case Some(text) => text
      case None =>
        val rawMessage = new scala.collection.mutable.StringBuilder(510)
        if (prefix.isDefined) rawMessage.append(prefix.get + "\u0020")
        rawMessage.append(command.getOrElse(numeric.get.id))

        // last parameter is the "trailing" one and starts with a colon
        parameters.zipWithIndex.foreach { case (parameter, i) =>
          val trailing = if (i == parameters.length - 1) ":" else ""
          rawMessage.append("\u0020" + trailing + parameter)
        }

        rawMessage.toString + "\r\n"
    }
  }

  def action: Boolean = {
    command match {
      case Some(Command.PRIVMSG) =>
        val text = parameters(1)
        if (text.startsWith("\u0001ACTION") && text.endsWith("\u0001"))
          true
        else
          false
      case _ => false
    }
  }

  def target: Option[String] = {
    command match {
      case Some(Command.PRIVMSG) => Option(parameters(0))
      case _ => None
    }
  }

  def contents: Option[String] = {
    command match {
      case Some(Command.PRIVMSG) =>
        Option(if (action) parameters(1).slice(8, parameters(1).length - 1) else parameters(1))
      case _ => None
    }
  }

  def pingValue: Option[String] = {
    command match {
      case Some(Command.PING) => Option(parameters(0))
      case _ => None
    }
  }
}

object Message {
  def apply(line: String): Message = {
    val (prefix, command, numeric, parameters) = fromRawMessageString(line)
    Message(raw        = Option(line),
            prefix     = prefix,
            command    = command,
            numeric    = numeric,
            parameters = parameters)
  }

  def apply(command: Command, parameters: String*): Message =
    Message(command = Option(command),
            parameters = parameters)

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
  def fromRawMessageString(text: String): (Option[String], Option[Command], Option[Numeric], Seq[String]) = {
    val tokens         = text.split("\u0020").map(_.trim)
    val prefix         = tokens(0)(0) match {
                           case ':' => Option(tokens(0))
                           case _ => None
                         }
    val commandOrReply = tokens(if (prefix.isDefined) 1 else 0).toUpperCase
    val command        = Try(Command.withName(commandOrReply)) match {
                           case Success(command) => Option(command)
                           case Failure(_) => None
                         }
    val numeric        = if (!command.isDefined) {
                           Try(Numeric.withName(commandOrReply)) match {
                             case Success(numeric) => Option(numeric)
                             case Failure(_) => Option(Numeric.UNKNOWN)
                           }
                         } else None
    val params         = tokens.drop(if (prefix.isDefined) 2 else 1).takeWhile(!_.startsWith(":"))
    val trailing       = text.split(":", 3).lastOption.map(_.stripLineEnd)

    (prefix, command, numeric, params ++ trailing)
  }
}
