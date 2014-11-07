package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class `Parses Privmsg` {
    @Test def `with a normal message` = {
      Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n") match {
        case message: Privmsg =>
          message.must(be(
            Privmsg(Option(":nick!ident@host.name PRIVMSG target :This is a message\r\n"),
                    Option(":nick!ident@host.name"),
                    Seq("target", "This is a message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with a colon in the message` = {
      Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n") match {
        case message: Privmsg =>
          message.must(be(
            Privmsg(Option(":nick!ident@host.name PRIVMSG target :This is a : message\r\n"),
                    Option(":nick!ident@host.name"),
                    Seq("target", "This is a : message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a : message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with whitespace without truncating it` = {
      Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n") match {
        case message: Privmsg =>
          message.must(be(
            Privmsg(Option(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n"),
                    Option(":nick!ident@host.name"),
                    Seq("target", "\t This is a message "))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("\t This is a message "))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with an action` = {
      Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n") match {
        case message: Privmsg =>
          message.must(be(
            Privmsg(Option(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n"),
                    Option(":nick!ident@host.name"),
                    Seq("target", "\u0001ACTION emotes\u0001"))))

          message.action.must(be(true))
          message.target.must(be("target"))
          message.contents.must(be("emotes"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }
  }

  class `Parses Reply` {
    @Test def `with WHO reply` = {
      Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n") match {
        case message: Reply =>
          message.must(be(
            Reply(Option(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n"),
                  Option(":irc.host"),
                  Option(Numeric.withName("352")),
                  Seq("someone", "#channel", "user", "0.0.0.0", "irc.host", "someone", "G", "0 Real Name"))))

        case _ => throw new Exception("Not a Reply")
      }
    }
  }

  class `Parses Ping` {
    @Test def `with hex value` = {
       Message("PING :8C4EF037\r\n") match {
        case message: Ping =>
          message.must(be(
            Ping(Option("PING :8C4EF037\r\n"), None, Seq("8C4EF037"))))
          message.pingValue.must(be("8C4EF037"))

        case _ => throw new Exception("Not a Ping")
      }
    }
  }

  class `Parses Mode` {
    @Test def `set by remote server` = {
       Message(":swayze MODE swayze :+i\r\n") match {
        case message: Mode =>
          message.must(be(
            Mode(Option(":swayze MODE swayze :+i\r\n"),
                 Option(":swayze"),
                 Seq("swayze", "+i"))))

        case _ => throw new Exception("Not a Mode")
      }
    }
  }
}
