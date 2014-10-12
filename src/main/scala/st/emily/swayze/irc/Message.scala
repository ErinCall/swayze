package st.emily.swayze.irc


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

object Numeric extends Enumeration {
  type Numeric = Value

  val RPL_WELCOME           = 1
  val RPL_YOURHOST          = 2
  val RPL_CREATED           = 3
  val RPL_MYINFO            = 4
  val RPL_BOUNCE            = 5
  val RPL_TRACELINK         = 200
  val RPL_TRACECONNECTING   = 201
  val RPL_TRACEHANDSHAKE    = 202
  val RPL_TRACEUNKNOWN      = 203
  val RPL_TRACEOPERATOR     = 204
  val RPL_TRACEUSER         = 205
  val RPL_TRACESERVER       = 206
  val RPL_TRACESERVICE      = 207
  val RPL_TRACENEWTYPE      = 208
  val RPL_TRACECLASS        = 209
  val RPL_TRACERECONNECT    = 210
  val RPL_STATSLINKINFO     = 211
  val RPL_STATSCOMMANDS     = 212
  val RPL_STATSCLINE        = 213
  val RPL_STATSNLINE        = 214
  val RPL_STATSILINE        = 215
  val RPL_STATSKLINE        = 216
  val RPL_STATSQLINE        = 217
  val RPL_STATSYLINE        = 218
  val RPL_ENDOFSTATS        = 219
  val RPL_UMODEIS           = 221
  val RPL_SERVICEINFO       = 231
  val RPL_ENDOFSERVICES     = 232
  val RPL_SERVICE           = 233
  val RPL_SERVLIST          = 234
  val RPL_SERVLISTEND       = 235
  val RPL_STATSVLINE        = 240
  val RPL_STATSLLINE        = 241
  val RPL_STATSUPTIME       = 242
  val RPL_STATSOLINE        = 243
  val RPL_STATSHLINE        = 244
  val RPL_STATSPING         = 246
  val RPL_STATSBLINE        = 247
  val RPL_STATSULINE        = 249
  val RPL_STATSDLINE        = 250
  val RPL_LUSERCLIENT       = 251
  val RPL_LUSEROP           = 252
  val RPL_LUSERUNKNOWN      = 253
  val RPL_LUSERCHANNELS     = 254
  val RPL_LUSERME           = 255
  val RPL_ADMINME           = 256
  val RPL_ADMINLOC1         = 257
  val RPL_ADMINLOC2         = 258
  val RPL_ADMINEMAIL        = 259
  val RPL_TRACELOG          = 261
  val RPL_TRACEEND          = 262
  val RPL_TRYAGAIN          = 263
  val RPL_NONE              = 300
  val RPL_AWAY              = 301
  val RPL_USERHOST          = 302
  val RPL_ISON              = 303
  val RPL_UNAWAY            = 305
  val RPL_NOWAWAY           = 306
  val RPL_WHOISUSER         = 311
  val RPL_WHOISSERVER       = 312
  val RPL_WHOISOPERATOR     = 313
  val RPL_WHOWASUSER        = 314
  val RPL_ENDOFWHO          = 315
  val RPL_WHOISCHANOP       = 316
  val RPL_WHOISIDLE         = 317
  val RPL_ENDOFWHOIS        = 318
  val RPL_WHOISCHANNELS     = 319
  val RPL_LISTSTART         = 321
  val RPL_LIST              = 322
  val RPL_LISTEND           = 323
  val RPL_CHANNELMODEIS     = 324
  val RPL_UNIQOPIS          = 325
  val RPL_NOTOPIC           = 331
  val RPL_TOPIC             = 332
  val RPL_INVITING          = 341
  val RPL_SUMMONING         = 342
  val RPL_INVITELIST        = 346
  val RPL_ENDOFINVITELIST   = 347
  val RPL_EXCEPTLIST        = 348
  val RPL_ENDOFEXCEPTLIST   = 349
  val RPL_VERSION           = 351
  val RPL_WHOREPLY          = 352
  val RPL_NAMREPLY          = 353
  val RPL_WHOSPCRPL         = 354
  val RPL_KILLDONE          = 361
  val RPL_CLOSING           = 362
  val RPL_CLOSEEND          = 363
  val RPL_LINKS             = 364
  val RPL_ENDOFLINKS        = 365
  val RPL_ENDOFNAMES        = 366
  val RPL_BANLIST           = 367
  val RPL_ENDOFBANLIST      = 368
  val RPL_ENDOFWHOWAS       = 369
  val RPL_INFO              = 371
  val RPL_MOTD              = 372
  val RPL_INFOSTART         = 373
  val RPL_ENDOFINFO         = 374
  val RPL_MOTDSTART         = 375
  val RPL_ENDOFMOTD         = 376
  val RPL_YOUREOPER         = 381
  val RPL_REHASHING         = 382
  val RPL_YOURESERVICE      = 383
  val RPL_MYPORTIS          = 384
  val RPL_TIME              = 391
  val RPL_USERSSTART        = 392
  val RPL_USERS             = 393
  val RPL_ENDOFUSERS        = 394
  val RPL_NOUSERS           = 395
  val ERR_NOSUCHNICK        = 401
  val ERR_NOSUCHSERVER      = 402
  val ERR_NOSUCHCHANNEL     = 403
  val ERR_CANNOTSENDTOCHAN  = 404
  val ERR_TOOMANYCHANNELS   = 405
  val ERR_WASNOSUCHNICK     = 406
  val ERR_TOOMANYTARGETS    = 407
  val ERR_NOSUCHSERVICE     = 408
  val ERR_NOORIGIN          = 409
  val ERR_NORECIPIENT       = 411
  val ERR_NOTEXTTOSEND      = 412
  val ERR_NOTOPLEVEL        = 413
  val ERR_WILDTOPLEVEL      = 414
  val ERR_BADMASK           = 415
  val ERR_UNKNOWNCOMMAND    = 421
  val ERR_NOMOTD            = 422
  val ERR_NOADMININFO       = 423
  val ERR_FILEERROR         = 424
  val ERR_NONICKNAMEGIVEN   = 431
  val ERR_ERRONEUSNICKNAME  = 432
  val ERR_NICKNAMEINUSE     = 433
  val ERR_NICKCOLLISION     = 436
  val ERR_UNAVAILRESOURCE   = 437
  val ERR_USERNOTINCHANNEL  = 441
  val ERR_NOTONCHANNEL      = 442
  val ERR_USERONCHANNEL     = 443
  val ERR_NOLOGIN           = 444
  val ERR_SUMMONDISABLED    = 445
  val ERR_USERSDISABLED     = 446
  val ERR_NOTREGISTERED     = 451
  val ERR_NEEDMOREPARAMS    = 461
  val ERR_ALREADYREGISTERED = 462
  val ERR_NOPERMFORHOST     = 463
  val ERR_PASSWDMISMATCH    = 464
  val ERR_YOUREBANNEDCREEP  = 465
  val ERR_YOUWILLBEBANNED   = 466
  val ERR_KEYSET            = 467
  val ERR_CHANNELISFULL     = 471
  val ERR_UNKNOWNMODE       = 472
  val ERR_INVITEONLYCHAN    = 473
  val ERR_BANNEDFROMCHAN    = 474
  val ERR_BADCHANNELKEY     = 475
  val ERR_BADCHANMASK       = 476
  val ERR_NOCHANMODES       = 477
  val ERR_BANLISTFULL       = 478
  val ERR_NOPRIVILEGES      = 481
  val ERR_CHANOPRIVSNEEDED  = 482
  val ERR_CANTKILLSERVER    = 483
  val ERR_RESTRICTED        = 484
  val ERR_UNIQOPRIVSNEEDED  = 485
  val ERR_NOOPERHOST        = 491
  val ERR_NOSERVICEHOST     = 492
  val ERR_UMODEUNKNOWNFLAG  = 501
  val ERR_USERSDONTMATCH    = 502
}

import Command.Command
import Numeric.Numeric


abstract sealed class Message(val prefix:     Option[String],
                              val command:    Command,
                              val parameters: Seq[String]) // up to 15 params

case class Privmsg(override val prefix:     Option[String],
                   override val command:    Command,
                   override val parameters: Seq[String],
                   action:                  Boolean,
                   target:                  String) extends Message(prefix, command, parameters)

case class Reply(override val prefix:     Option[String],
                 override val command:    Command,
                 override val parameters: Seq[String],
                 numeric:                 Numeric) extends Message(prefix, command, parameters)


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

