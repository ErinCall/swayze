package st.emily.swayze.irc


object Command extends Enumeration {
  type Command  = Value

  val AWAY     = Value
  val CONNECT  = Value
  val DIE      = Value
  val INFO     = Value
  val INVITE   = Value
  val ISON     = Value
  val JOIN     = Value
  val KICK     = Value
  val KILL     = Value
  val LINKS    = Value
  val LIST     = Value
  val MOTD     = Value
  val NAMES    = Value
  val NOTICE   = Value
  val NUMERIC  = Value  // internal command
  val OPER     = Value
  val PART     = Value
  val PASS     = Value
  val PING     = Value
  val PONG     = Value
  val PRIVMSG  = Value
  val QUIT     = Value
  val REHASH   = Value
  val RESTART  = Value
  val SERVICE  = Value
  val SERVLIST = Value
  val SQUERY   = Value
  val SQUIT    = Value
  val STATS    = Value
  val SUMMON   = Value
  val TIME     = Value
  val TOPIC    = Value
  val TRACE    = Value
  val UNKNOWN  = Value  // internal command
  val USER     = Value
  val USERHOST = Value
  val USERS    = Value
  val VERSION  = Value
  val WALLOPS  = Value
  val WHO      = Value
  val WHOIS    = Value
  val WHOWAS   = Value
}

import Command.Command


abstract sealed class Message(val prefix:     Option[String],
                              val command:    Command,
                              val parameters: Seq[String]) // up to 15 params

case class Privmsg(override val prefix:     Option[String],
                   override val command:    Command,
                   override val parameters: Seq[String],
                   action:                  Boolean,
                   target:                  String) extends Message(prefix, command, parameters)

case class Numeric(override val prefix:     Option[String],
                   override val command:    Command,
                   override val parameters: Seq[String],
                   code:                    Short) extends Message(prefix, command, parameters)


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

