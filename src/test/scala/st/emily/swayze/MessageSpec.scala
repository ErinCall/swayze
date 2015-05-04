package st.emily.swayze.irc

import st.emily.swayze.tests.SwayzeSpec

import Command._
import Numeric._


class MessageSpec extends SwayzeSpec {
  "A Message" - {
    "sent by a server" - {
      "should make a valid PRIVMSG raw string" in {
        val message = new Message(prefix     = Option("nick!ident@host.name"),
                                  command    = Option(PRIVMSG),
                                  numeric    = None,
                                  parameters = Seq("target", "This is a message"))
        message.toString should be (":nick!ident@host.name PRIVMSG target :This is a message\r\n")
      }

      "should make a valid REPLY raw string" in {
        val message = new Message(prefix     = Option("irc.host"),
                                  command    = None,
                                  numeric    = Option(RPL_WELCOME),
                                  parameters = Seq("This is a welcome message"))
        message.toString should be (":irc.host 001 :This is a welcome message\r\n")
      }
    }

    "sent by a client" - {
      "should make a valid JOIN raw string" in {
        val message = new Message(prefix     = None,
                                  command    = Option(JOIN),
                                  numeric    = None,
                                  parameters = Seq("#channel"))
        message.toString should be ("JOIN :#channel\r\n")
      }

      "should make a valid QUIT raw string" in {
        val message = new Message(prefix     = None,
                                  command    = Option(QUIT),
                                  numeric    = None,
                                  parameters = Seq())
        message.toString should be ("QUIT\r\n")
      }

      "should make a valid PRIVMSG raw string containing significant whitespace" in {
        val message = new Message(prefix     = None,
                                  command    = Option(PRIVMSG),
                                  numeric    = None,
                                  parameters = Seq("#target", " with significant  whitespace "))
        message.toString should be ("PRIVMSG #target : with significant  whitespace \r\n")
      }
    }

    "given a raw string" - {
      "from a server" - {
        "should parse a PRIVMSG" in {
          val message = Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")
          message should be(new Message(prefix     = Option("nick!ident@host.name"),
                                        command    = Option(PRIVMSG),
                                        numeric    = None,
                                        parameters = Seq("target", "This is a message")))
        }

        "should parse a PRIVMSG containing a colon" in {
          val message = Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n")
          message should be (new Message(prefix     = Option("nick!ident@host.name"),
                                         command    = Option(PRIVMSG),
                                         numeric    = None,
                                         parameters = Seq("target", "This is a : message")))
        }

        "should parse a PRIVMSG containing significant whitespace" in {
          val message = Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n")
          message should be (new Message(prefix     = Option("nick!ident@host.name"),
                                         command    = Option(PRIVMSG),
                                         numeric    = None,
                                         parameters = Seq("target", "\t This is a message ")))
        }

        "should parse a PRIVMSG containing an ACTION" in {
          val message = Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n")
          message should be (new Message(prefix     = Option("nick!ident@host.name"),
                                         command    = Option(PRIVMSG),
                                         numeric    = None,
                                         parameters = Seq("target", "\u0001ACTION emotes\u0001")))
        }

        "should parse a server reply (to WHO)" in {
          val message = Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n")
          message should be (new Message(prefix     = Option("irc.host"),
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

        "should parse a MODE" in {
          val message = Message(":swayze MODE swayze :+i\r\n")
          message should be (new Message(prefix     = Option("swayze"),
                                         command    = Option(MODE),
                                         numeric    = None,
                                         parameters = Seq("swayze", "+i")))
        }

        "should parse a NOTICE" in {
          val message = Message(":irc.server NOTICE AUTH :*** Looking up your hostname...\r\n")
          message should be (new Message(prefix     = Option("irc.server"),
                                         command    = Option(NOTICE),
                                         numeric    = None,
                                         parameters = Seq("AUTH", "*** Looking up your hostname...")))
        }
      }

      "from a client" - {
        "should parse a PING" in {
          val message = Message("PING :8C4EF037\r\n")
          message should be (new Message(prefix     = None,
                                         command    = Option(PING),
                                         numeric    = None,
                                         parameters = Seq("8C4EF037")))
        }

        "should parse a QUIT without a message" in {
          val message = Message("QUIT\r\n")
          message should be (new Message(prefix     = None,
                                         command    = Option(QUIT),
                                         numeric    = None,
                                         parameters = Seq()))
        }
      }
    }
  }
}
