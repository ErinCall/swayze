package st.emily.swayze.irc

import scala.collection.mutable.StringBuilder
import scala.util.{ Failure, Success, Try }

import st.emily.swayze.exceptions.FailedParseException


object Command extends Enumeration {
  type Command = Value

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

object Numeric extends Enumeration {
  type Numeric = Value

  val RPL_WELCOME           = Value("001")
  val RPL_YOURHOST          = Value("002")
  val RPL_CREATED           = Value("003")
  val RPL_MYINFO            = Value("004")
  val RPL_BOUNCE            = Value("005")
  val RPL_TRACELINK         = Value("200")
  val RPL_TRACECONNECTING   = Value("201")
  val RPL_TRACEHANDSHAKE    = Value("202")
  val RPL_TRACEUNKNOWN      = Value("203")
  val RPL_TRACEOPERATOR     = Value("204")
  val RPL_TRACEUSER         = Value("205")
  val RPL_TRACESERVER       = Value("206")
  val RPL_TRACESERVICE      = Value("207")
  val RPL_TRACENEWTYPE      = Value("208")
  val RPL_TRACECLASS        = Value("209")
  val RPL_TRACERECONNECT    = Value("210")
  val RPL_STATSLINKINFO     = Value("211")
  val RPL_STATSCOMMANDS     = Value("212")
  val RPL_STATSCLINE        = Value("213")
  val RPL_STATSNLINE        = Value("214")
  val RPL_STATSILINE        = Value("215")
  val RPL_STATSKLINE        = Value("216")
  val RPL_STATSQLINE        = Value("217")
  val RPL_STATSYLINE        = Value("218")
  val RPL_ENDOFSTATS        = Value("219")
  val RPL_UMODEIS           = Value("221")
  val RPL_SERVICEINFO       = Value("231")
  val RPL_ENDOFSERVICES     = Value("232")
  val RPL_SERVICE           = Value("233")
  val RPL_SERVLIST          = Value("234")
  val RPL_SERVLISTEND       = Value("235")
  val RPL_STATSVLINE        = Value("240")
  val RPL_STATSLLINE        = Value("241")
  val RPL_STATSUPTIME       = Value("242")
  val RPL_STATSOLINE        = Value("243")
  val RPL_STATSHLINE        = Value("244")
  val RPL_STATSPING         = Value("246")
  val RPL_STATSBLINE        = Value("247")
  val RPL_STATSULINE        = Value("249")
  val RPL_STATSDLINE        = Value("250")
  val RPL_LUSERCLIENT       = Value("251")
  val RPL_LUSEROP           = Value("252")
  val RPL_LUSERUNKNOWN      = Value("253")
  val RPL_LUSERCHANNELS     = Value("254")
  val RPL_LUSERME           = Value("255")
  val RPL_ADMINME           = Value("256")
  val RPL_ADMINLOC1         = Value("257")
  val RPL_ADMINLOC2         = Value("258")
  val RPL_ADMINEMAIL        = Value("259")
  val RPL_TRACELOG          = Value("261")
  val RPL_TRACEEND          = Value("262")
  val RPL_TRYAGAIN          = Value("263")
  val RPL_LOCALUSERS        = Value("265")
  val RPL_GLOBALUSERS       = Value("266")
  val RPL_NONE              = Value("300")
  val RPL_AWAY              = Value("301")
  val RPL_USERHOST          = Value("302")
  val RPL_ISON              = Value("303")
  val RPL_UNAWAY            = Value("305")
  val RPL_NOWAWAY           = Value("306")
  val RPL_WHOISUSER         = Value("311")
  val RPL_WHOISSERVER       = Value("312")
  val RPL_WHOISOPERATOR     = Value("313")
  val RPL_WHOWASUSER        = Value("314")
  val RPL_ENDOFWHO          = Value("315")
  val RPL_WHOISCHANOP       = Value("316")
  val RPL_WHOISIDLE         = Value("317")
  val RPL_ENDOFWHOIS        = Value("318")
  val RPL_WHOISCHANNELS     = Value("319")
  val RPL_LISTSTART         = Value("321")
  val RPL_LIST              = Value("322")
  val RPL_LISTEND           = Value("323")
  val RPL_CHANNELMODEIS     = Value("324")
  val RPL_UNIQOPIS          = Value("325")
  val RPL_NOTOPIC           = Value("331")
  val RPL_TOPIC             = Value("332")
  val RPL_TOPICWHOTIME      = Value("333")
  val RPL_INVITING          = Value("341")
  val RPL_SUMMONING         = Value("342")
  val RPL_INVITELIST        = Value("346")
  val RPL_ENDOFINVITELIST   = Value("347")
  val RPL_EXCEPTLIST        = Value("348")
  val RPL_ENDOFEXCEPTLIST   = Value("349")
  val RPL_VERSION           = Value("351")
  val RPL_WHOREPLY          = Value("352")
  val RPL_NAMREPLY          = Value("353")
  val RPL_WHOSPCRPL         = Value("354")
  val RPL_KILLDONE          = Value("361")
  val RPL_CLOSING           = Value("362")
  val RPL_CLOSEEND          = Value("363")
  val RPL_LINKS             = Value("364")
  val RPL_ENDOFLINKS        = Value("365")
  val RPL_ENDOFNAMES        = Value("366")
  val RPL_BANLIST           = Value("367")
  val RPL_ENDOFBANLIST      = Value("368")
  val RPL_ENDOFWHOWAS       = Value("369")
  val RPL_INFO              = Value("371")
  val RPL_MOTD              = Value("372")
  val RPL_INFOSTART         = Value("373")
  val RPL_ENDOFINFO         = Value("374")
  val RPL_MOTDSTART         = Value("375")
  val RPL_ENDOFMOTD         = Value("376")
  val RPL_YOUREOPER         = Value("381")
  val RPL_REHASHING         = Value("382")
  val RPL_YOURESERVICE      = Value("383")
  val RPL_MYPORTIS          = Value("384")
  val RPL_TIME              = Value("391")
  val RPL_USERSSTART        = Value("392")
  val RPL_USERS             = Value("393")
  val RPL_ENDOFUSERS        = Value("394")
  val RPL_NOUSERS           = Value("395")
  val ERR_NOSUCHNICK        = Value("401")
  val ERR_NOSUCHSERVER      = Value("402")
  val ERR_NOSUCHCHANNEL     = Value("403")
  val ERR_CANNOTSENDTOCHAN  = Value("404")
  val ERR_TOOMANYCHANNELS   = Value("405")
  val ERR_WASNOSUCHNICK     = Value("406")
  val ERR_TOOMANYTARGETS    = Value("407")
  val ERR_NOSUCHSERVICE     = Value("408")
  val ERR_NOORIGIN          = Value("409")
  val ERR_NORECIPIENT       = Value("411")
  val ERR_NOTEXTTOSEND      = Value("412")
  val ERR_NOTOPLEVEL        = Value("413")
  val ERR_WILDTOPLEVEL      = Value("414")
  val ERR_BADMASK           = Value("415")
  val ERR_UNKNOWNCOMMAND    = Value("421")
  val ERR_NOMOTD            = Value("422")
  val ERR_NOADMININFO       = Value("423")
  val ERR_FILEERROR         = Value("424")
  val ERR_NONICKNAMEGIVEN   = Value("431")
  val ERR_ERRONEUSNICKNAME  = Value("432")
  val ERR_NICKNAMEINUSE     = Value("433")
  val ERR_NICKCOLLISION     = Value("436")
  val ERR_UNAVAILRESOURCE   = Value("437")
  val ERR_USERNOTINCHANNEL  = Value("441")
  val ERR_NOTONCHANNEL      = Value("442")
  val ERR_USERONCHANNEL     = Value("443")
  val ERR_NOLOGIN           = Value("444")
  val ERR_SUMMONDISABLED    = Value("445")
  val ERR_USERSDISABLED     = Value("446")
  val ERR_NOTREGISTERED     = Value("451")
  val ERR_NEEDMOREPARAMS    = Value("461")
  val ERR_ALREADYREGISTERED = Value("462")
  val ERR_NOPERMFORHOST     = Value("463")
  val ERR_PASSWDMISMATCH    = Value("464")
  val ERR_YOUREBANNEDCREEP  = Value("465")
  val ERR_YOUWILLBEBANNED   = Value("466")
  val ERR_KEYSET            = Value("467")
  val ERR_CHANNELISFULL     = Value("471")
  val ERR_UNKNOWNMODE       = Value("472")
  val ERR_INVITEONLYCHAN    = Value("473")
  val ERR_BANNEDFROMCHAN    = Value("474")
  val ERR_BADCHANNELKEY     = Value("475")
  val ERR_BADCHANMASK       = Value("476")
  val ERR_NOCHANMODES       = Value("477")
  val ERR_BANLISTFULL       = Value("478")
  val ERR_NOPRIVILEGES      = Value("481")
  val ERR_CHANOPRIVSNEEDED  = Value("482")
  val ERR_CANTKILLSERVER    = Value("483")
  val ERR_RESTRICTED        = Value("484")
  val ERR_UNIQOPRIVSNEEDED  = Value("485")
  val ERR_NOOPERHOST        = Value("491")
  val ERR_NOSERVICEHOST     = Value("492")
  val ERR_UMODEUNKNOWNFLAG  = Value("501")
  val ERR_USERSDONTMATCH    = Value("502")
}

import Command._
import Numeric._

case class Message(val prefix:     Option[String]  = None,
                   val command:    Option[Command] = None,
                   val numeric:    Option[Numeric] = None,
                   val parameters: Seq[String]     = Seq()) {
  override lazy val toString: String = {
    (Seq(prefix.map("\u003A" + _), numeric, command) ++
      parameters.dropRight(1).map(Option(_)) :+
      parameters.lastOption.map("\u003A" + _)).flatten.mkString("\u0020") + "\u000D\u000A"
  }
}

object Message {
  def apply(line: String) = {
    val lexemes = line.split("(?<=\u0020)") // keep whitespace (for trailing param)
    val (prefix, command, params, trailing) = (lexemes.headOption, lexemes.tail) match {
      case (Some(x), xs) if x.startsWith("\u003A") => (
        Option(x.tail.trim),
        xs.head.trim,
        xs.tail.takeWhile(!_.startsWith("\u003A")).map(_.trim),
        xs.tail.dropWhile(!_.startsWith("\u003A")).mkString.stripPrefix("\u003A").stripSuffix("\u000D\u000A")
      )

      case (Some(x), xs) => (
        None,
        x.trim,
        xs.takeWhile(!_.startsWith("\u003A")).map(_.trim),
        xs.dropWhile(!_.startsWith("\u003A")).mkString.stripPrefix("\u003A").stripSuffix("\u000D\u000A")
      )

      case _ =>
        throw FailedParseException(f"Couldn't parse line to message: `$line`")
    }

    (Try(Command.withName(command)), Try(Numeric.withName(command))) match {
      case (Failure(_), Success(numeric)) =>
        new Message(prefix     = prefix,
                    numeric    = Option(numeric),
                    parameters = if (trailing.isEmpty) params else params :+ trailing)

      case (Success(command), Failure(_)) =>
        new Message(prefix     = prefix,
                    command    = Option(command),
                    parameters = if (trailing.isEmpty) params else params :+ trailing)

      case (_, _)  =>
        throw FailedParseException(f"Unknown kind of message while parsing: `$line`")
    }
  }

  def apply(command: Command) = new Message(None, Option(command), None, Seq())

  def apply(command: Command, params: String*) = new Message(None, Option(command), None, params)
}
