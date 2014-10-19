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
 * rid of eventually.
 */
case class Message(raw:        Option[String],
                   prefix:     Option[String],
                   command:    Option[Command],
                   numeric:    Option[Numeric],
                   parameters: Seq[String]) {
  def toRaw = raw.getOrElse {
    (Seq(prefix.getOrElse(""), command.getOrElse(numeric.get.id)) ++ parameters).mkString("\u0020") + "\r\n"
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
      case Some(Command.PRIVMSG) =>
        Option(parameters(0))
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
}

object Message {
  def apply(line: String): Message = {
    val (prefix, command, numeric, parameters) = parse(line)
    new Message(Option(line), prefix, command, numeric, parameters)
  }

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
  def parse(text: String): (Option[String], Option[Command], Option[Numeric], Seq[String]) = {
    val tokens         = text.split("\u0020").map(_.trim)
    val prefix         = tokens(0)(0) match {
                           case ':' => Option(tokens(0))
                           case _   => None
                         }
    val commandOrReply = tokens(if (prefix.isDefined) 1 else 0).toUpperCase
    val command        = Try(Command.withName(commandOrReply)) match {
                           case Success(command) => Option(command)
                           case Failure(_)       => None
                         }
    val numeric        = if (!command.isDefined) Option(Numeric.withName(commandOrReply)) else None
    val params         = tokens.drop(if (prefix.isDefined) 2 else 1).takeWhile(!_.startsWith(":"))
    val trailing       = text.split(":", 3).lastOption.map(_.stripLineEnd)

    (prefix, command, numeric, params ++ trailing)
  }
}
