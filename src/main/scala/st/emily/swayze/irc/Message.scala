package st.emily.swayze.irc

import scala.util.{ Failure, Success, Try }

import st.emily.swayze.exceptions.FailedParseException
import Command.Command
import Numeric.Numeric


/**
 * Base trait for all IRC messages.
 */
trait Message {
  val prefix: Option[String]
  val command: Command = Command.UNKNOWN
  val numeric: Numeric = Numeric.UNKNOWN
  val parameters: Seq[String]

  override lazy val toString = {
    val rawMessage = new scala.collection.mutable.StringBuilder(510) // TODO: enforce this limit
    if (prefix.isDefined) rawMessage.append(prefix.get + "\u0020")
    rawMessage.append(if (command == Command.REPLY) numeric else command)

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
      val (prefix, command, numeric, parameters) = fromString(line)

      val message = command.getOrElse(Command.REPLY) match {
        case Command.REPLY   => Reply(prefix, numeric.get, parameters)

        case Command.PRIVMSG => Privmsg(prefix, parameters)
        case Command.PING    => Ping(prefix, parameters)
        case Command.PONG    => Pong(prefix, parameters)
        case Command.MODE    => Mode(prefix, parameters)
        case Command.JOIN    => Join(prefix, parameters)
        case Command.NICK    => Nick(prefix, parameters)
        case Command.NOTICE  => Notice(prefix, parameters)
      }

      message
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
  private[this] def fromString(text: String): (Option[String], Option[Command], Option[Numeric], Seq[String]) = {
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

/**
 * Represents a server reply message.
 *
 * Represents a reply from the server. Every reply has a numerical code
 * which represents the kind of reply, usually with parameters specific
 * to that reply code.
 */
case class Reply(val prefix: Option[String] = None,
                 override val numeric: Numeric,
                 val parameters: Seq[String]) extends Message {
  override val command = Command.REPLY
}

/**
 * Represents a PRIVMSG message
 *
 * Privmsg commands send messages from one user to other users or to
 * channels.
 */
case class Privmsg(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  require(parameters.size == 2, "A Privmsg must have a target and content")

  override val command = Command.PRIVMSG

  lazy val action: Boolean = parameters(1).startsWith("\u0001ACTION")
  lazy val target: String = parameters(0)
  lazy val contents: String =
    if (action) {
      parameters(1).slice(8, parameters(1).length - 1)
    } else {
      parameters(1)
    }
}

object Privmsg { def apply(parameters: Seq[String]): Privmsg = Privmsg(None, parameters) }

/**
 * Represents a PING message.
 *
 * Ping commands are commonly used by the server to determine if a user
 * has disconnected, sent at a regular interval. Users may also send
 * pings to the server.
 */
case class Ping(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Ping must have a value")

  override val command = Command.PING

  lazy val pingValue: String = parameters(0)
}

object Ping { def apply(parameters: Seq[String]): Ping = Ping(None, parameters) }

/**
 * Represents a PONG message.
 *
 * Clients must respond to PING commands with a PONG using the same
 * value before the server-configured timeout.
 */
case class Pong(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Pong must have a value")

  override val command = Command.PONG

  lazy val pongValue: String = parameters(0)
}

object Pong { def apply(parameters: Seq[String]): Pong = Pong(None, parameters) }

/**
 * Represents a MODE message.
 *
 * Used by clients to set the mode for either a user or channel. Used
 * by the server to report modes (and changes to modes) to clients.
 */
case class Mode(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  require(parameters.size == 2, "A Mode must have a target and a mode")

  override val command = Command.MODE

  lazy val target: String = parameters(0)
  lazy val mode: String = parameters(1)
}

object Mode { def apply(parameters: Seq[String]): Mode = Mode(None, parameters) }

/**
 * Represents a NICK message.
 *
 * Used by clients to set their nickname. Also used by clients as the
 * first command sent during logging in on initial connection (if the
 * server doesn't require a password). Used by the server to report
 * nickname changes.
 */
case class Nick(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  override val command = Command.NICK

  lazy val nickname: String = parameters(0)
}

object Nick { def apply(parameters: Seq[String]): Nick = Nick(None, parameters) }

/**
 * Represents a USER message.
 *
 * Second command used by clients during the login process, used to set
 * ancillary personal information (ident name and real name).
 */
case class User(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  // require(parameters.size == 1, "A Nick must have a nickname") // TODO handle server nickname commands

  override val command = Command.USER
}

object User { def apply(parameters: Seq[String]): User = User(None, parameters) }

/**
 * Represents a JOIN message.
 *
 * Used by clients to join channels. Used by servers to report users
 * joining a channel.
 */
case class Join(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Join must have a channel")

  override val command = Command.JOIN
}

object Join { def apply(parameters: Seq[String]): Join = Join(None, parameters) }

/**
 * Represents a NOTICE message.
 *
 * Used by clients and servers to send out-of-band information to
 * users.
 */
case class Notice(val prefix: Option[String] = None, val parameters: Seq[String]) extends Message {
  override val command = Command.NOTICE
}

object Notice { def apply(parameters: Seq[String]): Notice = Notice(None, parameters) }
