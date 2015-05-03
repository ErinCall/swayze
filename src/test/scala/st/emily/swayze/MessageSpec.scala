package st.emily.swayze.irc

import st.emily.swayze.tests.SwayzeSpec

import Command._
import Numeric._


class MessageSpec extends SwayzeSpec {
  "A Message" - {
    "when turned to a raw string" - {
      "should make a valid server PRIVMSG" in {
        val message = new Message(prefix     = Option("nick!ident@host.name"),
                                  command    = Option(PRIVMSG),
                                  numeric    = None,
                                  parameters = Seq("target", "This is a message"))
        message.toString should be (":nick!ident@host.name PRIVMSG target :This is a message\r\n")
      }

      "should make a valid server REPLY" in {
        val message = new Message(prefix     = Option("irc.host"),
                                  command    = None,
                                  numeric    = Option(RPL_WELCOME),
                                  parameters = Seq("This is a welcome message"))
        message.toString should be (":irc.host 001 :This is a welcome message\r\n")
      }

      "should make a valid client JOIN" in {
        val message = new Message(prefix     = None,
                                  command    = Option(JOIN),
                                  numeric    = None,
                                  parameters = Seq("#channel"))
        message.toString should be ("JOIN :#channel\r\n")
      }

      "should make a valid client QUIT without a message" in {
        val message = new Message(prefix     = None,
                                  command    = Option(QUIT),
                                  numeric    = None,
                                  parameters = Seq())
        message.toString should be ("QUIT\r\n")
      }

      "should make a valid client PRIVMSG containing significant whitespace" in {
        val message = new Message(prefix     = None,
                                  command    = Option(PRIVMSG),
                                  numeric    = None,
                                  parameters = Seq("#target", " with significant  whitespace "))
        message.toString should be ("PRIVMSG #target : with significant  whitespace \r\n")
      }
    }

    "given a raw string" - {
      "should parse a server PRIVMSG" in {
        Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n") should be
          (new Message(prefix     = Option("nick!ident@host.name"),
                       command    = Option(PRIVMSG),
                       numeric    = None,
                       parameters = Seq("target", "This is a message")))
      }

      "should parse a server PRIVMSG containing a colon" in {
        Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n") should be
          (new Message(prefix     = Option("nick!ident@host.name"),
                       command    = Option(PRIVMSG),
                       numeric    = None,
                       parameters = Seq("target", "This is a : message")))
      }

      "should parse a server PRIVMSG containing significant whitespace" in {
        Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n") should be
          (new Message(prefix     = Option("nick!ident@host.name"),
                       command    = Option(PRIVMSG),
                       numeric    = None,
                       parameters = Seq("target", "\t This is a message ")))
      }

      "should parse a server PRIVMSG containing an ACTION" in {
        Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n") should be
          (new Message(prefix     = Option("nick!ident@host.name"),
                       command    = Option(PRIVMSG),
                       numeric    = None,
                       parameters = Seq("target", "\u0001ACTION emotes\u0001")))
      }

      "should parse a server reply (to WHO)" in {
        Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n") should be
          (new Message(prefix     = Option("irc.host"),
                       command    = None,
                       numeric    = Option(RPL_WHOREPLY),
                       parameters = Seq("someone",
                                        "#channel",
                                        "user",
                                        "0.0.0.0",
                                        "irc.host",
                                        "someone",
                                        "G",
                                        "0 Real Name")))
      }

      "should parse a client PING" in {
        Message("PING :8C4EF037\r\n") should be
          (new Message(prefix     = None,
                       command    = Option(PING),
                       numeric    = None,
                       parameters = Seq("8C4EF037")))
      }

      "should parse a server MODE" in {
        Message(":swayze MODE swayze :+i\r\n") should be
          (new Message(prefix     = Option("swayze"),
                       command    = Option(MODE),
                       numeric    = None,
                       parameters = Seq("swayze", "+i")))
      }

      "should parse a server NOTICE" in {
        Message(":irc.server NOTICE AUTH :*** Looking up your hostname...\r\n") should be
          (new Message(prefix     = Option("irc.server"),
                       command    = Option(NOTICE),
                       numeric    = None,
                       parameters = Seq("AUTH", "*** Looking up your hostname...")))
      }

      "should parse a client QUIT without a message" in {
        Message("QUIT\r\n") should be
          (new Message(prefix     = None,
                       command    = Option(QUIT),
                       numeric    = None,
                       parameters = Seq()))
      }
    }
  }
}
