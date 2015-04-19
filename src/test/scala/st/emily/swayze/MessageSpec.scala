package st.emily.swayze.irc

import st.emily.swayze.tests.SwayzeSpec

import Command._
import Numeric._


class MessageSpec extends SwayzeSpec {
  describe("A Message") {
    describe("when turned to a raw string") {
      it("should make a valid server PRIVMSG") {
        new Message(prefix     = Option("nick!ident@host.name"),
                    command    = Option(PRIVMSG),
                    numeric    = None,
                    parameters = Seq("target", "This is a message")).toString
          .should(be(":nick!ident@host.name PRIVMSG target :This is a message\r\n"))
      }

      it("should make a valid server REPLY") {
        new Message(prefix     = Option("irc.host"),
                    command    = None,
                    numeric    = Option(RPL_WELCOME),
                    parameters = Seq("This is a welcome message")).toString
          .should(be(":irc.host 001 :This is a welcome message\r\n"))
      }

      it("should make a valid client JOIN") {
        new Message(prefix     = None,
                    command    = Option(JOIN),
                    numeric    = None,
                    parameters = Seq("#channel")).toString
          .should(be("JOIN :#channel\r\n"))
      }

      it("should make a valid client QUIT without a message") {
        new Message(prefix     = None,
                    command    = Option(QUIT),
                    numeric    = None,
                    parameters = Seq()).toString
          .should(be("QUIT\r\n"))
      }

      it("should make a valid client PRIVMSG containing significant whitespace") {
        new Message(prefix     = None,
                    command    = Option(PRIVMSG),
                    numeric    = None,
                    parameters = Seq("#target", " with significant  whitespace ")).toString
          .should(be("PRIVMSG #target : with significant  whitespace \r\n"))
      }
    }

    describe("given a raw string") {
      it("should parse a server PRIVMSG") {
        Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")
          .should(be(new Message(prefix     = Option("nick!ident@host.name"),
                                 command    = Option(PRIVMSG),
                                 numeric    = None,
                                 parameters = Seq("target", "This is a message"))))
      }

      it("should parse a server PRIVMSG containing a colon") {
        Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n")
          .should(be(new Message(prefix     = Option("nick!ident@host.name"),
                                 command    = Option(PRIVMSG),
                                 numeric    = None,
                                 parameters = Seq("target", "This is a : message"))))
      }

      it("should parse a server PRIVMSG containing significant whitespace") {
        Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n")
          .should(be(new Message(prefix     = Option("nick!ident@host.name"),
                                 command    = Option(PRIVMSG),
                                 numeric    = None,
                                 parameters = Seq("target", "\t This is a message "))))
      }

      it("should parse a server PRIVMSG containing an ACTION") {
        Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n")
          .should(be(new Message(prefix     = Option("nick!ident@host.name"),
                                 command    = Option(PRIVMSG),
                                 numeric    = None,
                                 parameters = Seq("target", "\u0001ACTION emotes\u0001"))))
      }

      it("should parse a server reply (to WHO)") {
        Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n")
          .should(be(new Message(prefix     = Option("irc.host"),
                                 command    = None,
                                 numeric    = Option(RPL_WHOREPLY),
                                 parameters = Seq("someone",
                                                  "#channel",
                                                  "user",
                                                  "0.0.0.0",
                                                  "irc.host",
                                                  "someone",
                                                  "G",
                                                  "0 Real Name"))))
      }

      it("should parse a client PING") {
        Message("PING :8C4EF037\r\n")
          .should(be(new Message(prefix     = None,
                                 command    = Option(PING),
                                 numeric    = None,
                                 parameters = Seq("8C4EF037"))))
      }

      it("should parse a server MODE") {
        Message(":swayze MODE swayze :+i\r\n")
          .should(be(new Message(prefix     = Option("swayze"),
                                 command    = Option(MODE),
                                 numeric    = None,
                                 parameters = Seq("swayze", "+i"))))
      }

      it("should parse a server NOTICE") {
        Message(":irc.server NOTICE AUTH :*** Looking up your hostname...\r\n")
          .should(be(new Message(prefix     = Option("irc.server"),
                                 command    = Option(NOTICE),
                                 numeric    = None,
                                 parameters = Seq("AUTH", "*** Looking up your hostname..."))))
      }

      it("should parse a client QUIT without a message") {
        Message("QUIT\r\n")
          .should(be(new Message(prefix     = None,
                                 command    = Option(QUIT),
                                 numeric    = None,
                                 parameters = Seq())))
      }
    }
  }
}
