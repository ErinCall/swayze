package st.emily.swayze.irc

import scala.collection.mutable.StringBuilder
import scala.util.{ Failure, Success, Try }

import st.emily.swayze.exceptions.FailedParseException
import Command.Command
import Numeric.Numeric


/**
 * Base trait for all IRC messages.
 */
sealed abstract class Message {
  val prefix: Option[String]
  val command: Command = Command.UNKNOWN
  val numeric: Numeric = Numeric.UNKNOWN
  val parameters: Seq[String]

  override lazy val toString: String = {
    val message = new StringBuilder(510) // TODO: enforce this limit in bytes

    prefix.foreach { p => message.append(":" + p + "\u0020") }
    message.append(if (command == Command.REPLY) numeric else command)
    parameters.slice(0, parameters.length - 1).foreach { p => message.append("\u0020" + p) }
    parameters.lastOption.foreach { p => message.append("\u0020:" + p) }

    message.toString + "\u000D\u000A"
  }
}

object Message {
  def apply(line: String): Message = {
    val lexemes = line.split("(?<=\u0020)") // keep whitespace (for trailing param)
    val (prefix, command, params, trailing) = (lexemes.headOption, lexemes.tail) match {
      case (Some(x), xs) if x.startsWith(":") => (
        Option(x.tail.trim),
        xs.head.trim,
        xs.tail.takeWhile(!_.startsWith(":")).map(_.trim),
        xs.tail.dropWhile(!_.startsWith(":")).mkString.stripPrefix(":").stripSuffix("\u000D\u000A")
      )

      case (Some(x), xs) => (
        None,
        x.trim,
        xs.takeWhile(!_.startsWith(":")).map(_.trim),
        xs.dropWhile(!_.startsWith(":")).mkString.stripPrefix(":").stripSuffix("\u000D\u000A")
      )

      case _ =>
        throw FailedParseException(f"Couldn't parse line to message: `$line`")
    }

    val paramsWithTrailing = if (trailing.isEmpty) params else params :+ trailing

    (Try(Command.withName(command)), Try(Numeric.withName(command))) match {
      case (Failure(_), Success(numeric))         => Reply(prefix, numeric, paramsWithTrailing)

      case (Success(Command.PRIVMSG), Failure(_)) => Privmsg(prefix, paramsWithTrailing)
      case (Success(Command.PING), Failure(_))    => Ping(prefix, paramsWithTrailing)
      case (Success(Command.PONG), Failure(_))    => Pong(prefix, paramsWithTrailing)
      case (Success(Command.MODE), Failure(_))    => Mode(prefix, paramsWithTrailing)
      case (Success(Command.JOIN), Failure(_))    => Join(prefix, paramsWithTrailing)
      case (Success(Command.NICK), Failure(_))    => Nick(prefix, paramsWithTrailing)
      case (Success(Command.NOTICE), Failure(_))  => Notice(prefix, paramsWithTrailing)
      case (Success(Command.QUIT), Failure(_))    => Quit(prefix, paramsWithTrailing)

      case (_, _)  =>
        throw FailedParseException(f"Unknown kind of message while parsing: `$line`")
    }
  }
}

trait Targetable {
  val parameters: Seq[String]
  lazy val target: String = parameters(0)
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
case class Privmsg(val prefix: Option[String] = None,
                   val parameters: Seq[String]) extends Message with Targetable {
  require(parameters.size == 2, "A Privmsg must have a target and content")

  override val command = Command.PRIVMSG

  lazy val action: Boolean = parameters(1).startsWith("\u0001ACTION")
  lazy val contents: String = if (action) {
    parameters(1).slice(8, parameters(1).length - 1)
  } else {
    parameters(1)
  }
}

object Privmsg {
  def apply(parameters: Seq[String]): Privmsg = Privmsg(None, parameters)
}

/**
 * Represents a PING message.
 *
 * Ping commands are commonly used by the server to determine if a user
 * has disconnected, sent at a regular interval. Users may also send
 * pings to the server.
 */
case class Ping(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Ping must have a value")

  override val command = Command.PING

  lazy val pingValue: String = parameters(0)
}

object Ping {
  def apply(parameters: Seq[String]): Ping = Ping(None, parameters)
}

/**
 * Represents a PONG message.
 *
 * Clients must respond to PING commands with a PONG using the same
 * value before the server-configured timeout.
 */
case class Pong(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Pong must have a value")

  override val command = Command.PONG

  lazy val pongValue: String = parameters(0)
}

object Pong {
  def apply(parameters: Seq[String]): Pong = Pong(None, parameters)
}

/**
 * Represents a MODE message.
 *
 * Used by clients to set the mode for either a user or channel. Used
 * by the server to report modes (and changes to modes) to clients.
 */
case class Mode(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message with Targetable {
  require(parameters.size == 2, "A Mode must have a target and a mode")

  override val command = Command.MODE

  lazy val mode: String = parameters(1)
}

object Mode {
  def apply(parameters: Seq[String]): Mode = Mode(None, parameters)
}

/**
 * Represents a NICK message.
 *
 * Used by clients to set their nickname. Also used by clients as the
 * first command sent during logging in on initial connection (if the
 * server doesn't require a password). Used by the server to report
 * nickname changes.
 */
case class Nick(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message {
  override val command = Command.NICK

  lazy val nickname: String = parameters(0)
}

object Nick {
  def apply(parameters: Seq[String]): Nick = Nick(None, parameters)
}

/**
 * Represents a USER message.
 *
 * Second command used by clients during the login process, used to set
 * ancillary personal information (ident name and real name).
 */
case class User(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message {
  // require(parameters.size == 1, "A Nick must have a nickname") // TODO handle server nickname commands

  override val command = Command.USER
}

object User {
  def apply(parameters: Seq[String]): User = User(None, parameters)
}

/**
 * Represents a JOIN message.
 *
 * Used by clients to join channels. Used by servers to report users
 * joining a channel.
 */
case class Join(val prefix: Option[String] = None,
                val parameters: Seq[String]) extends Message {
  require(parameters.size == 1, "A Join must have a channel")

  override val command = Command.JOIN
}

object Join {
  def apply(parameters: Seq[String]): Join = Join(None, parameters)
}

/**
 * Represents a NOTICE message.
 *
 * Used by clients and servers to send out-of-band information to
 * users.
 */
case class Notice(val prefix: Option[String] = None,
                  val parameters: Seq[String])
extends Message {
  override val command = Command.NOTICE
}

object Notice {
  def apply(parameters: Seq[String]): Notice = Notice(None, parameters)
}

/**
 * Represents a QUIT message.
 *
 * Used by clients and servers to advise users and the server that the
 * client is preparing to terminate the connection.
 */
case class Quit(val prefix: Option[String] = None,
                val parameters: Seq[String] = Seq()) extends Message {
  override val command = Command.QUIT
}

object Quit {
  def apply(parameters: Seq[String]): Quit = Quit(None, parameters)
}
