package st.emily.swayze.irc


object Command extends Enumeration {
  type Command = Value

  val REPLY    = Value  // internal command
  val UNKNOWN  = Value  // internal command

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
  val MODE     = Value
  val MOTD     = Value
  val NICK     = Value
  val NAMES    = Value
  val NOTICE   = Value
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
  val USER     = Value
  val USERHOST = Value
  val USERS    = Value
  val VERSION  = Value
  val WALLOPS  = Value
  val WHO      = Value
  val WHOIS    = Value
  val WHOWAS   = Value
}

