package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class `Message can toString` {
    @Test def `with a server-originated Privmsg` = {
      val message = Privmsg(prefix     = Option(":nick!ident@host.name"),
                            parameters = Seq("target", "This is a message"))
      message.toString.must(be(":nick!ident@host.name PRIVMSG target :This is a message\r\n"))
    }

    @Test def `with a server-originated reply` = {
      val message = Reply(prefix     = Option(":nick!ident@host.name"),
                          numeric    = Some(Numeric.RPL_WELCOME),
                          parameters = Seq("This is a welcome message"))
      message.toString.must(be(":nick!ident@host.name 001 :This is a welcome message\r\n"))
    }

    @Test def `with a client-originated Join` = {
      val message = Join(prefix     = None,
                         parameters = Seq("#channel"))
      message.toString.must(be("JOIN :#channel\r\n"))
    }
  }

  class `Parses Privmsg` {
    @Test def `with a normal message` = {
      Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(prefix     = Option(":nick!ident@host.name"),
                                  parameters = Seq("target", "This is a message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with a colon in the message` = {
      Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(prefix     = Option(":nick!ident@host.name"),
                                  parameters = Seq("target", "This is a : message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a : message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with whitespace without truncating it` = {
      Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(prefix     = Option(":nick!ident@host.name"),
                                  parameters = Seq("target", "\t This is a message "))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("\t This is a message "))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def `with an action` = {
      Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(prefix     = Option(":nick!ident@host.name"),
                                  parameters = Seq("target", "\u0001ACTION emotes\u0001"))))

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
          message.must(be(Reply(prefix     = Option(":irc.host"),
                                numeric    = Option(Numeric.RPL_WHOREPLY),
                                parameters = Seq("someone",
                                                 "#channel",
                                                 "user",
                                                 "0.0.0.0",
                                                 "irc.host",
                                                 "someone",
                                                 "G",
                                                 "0 Real Name"))))

        case _ => throw new Exception("Not a Reply")
      }
    }
  }

  class `Parses Ping` {
    @Test def `with hex value` = {
       Message("PING :8C4EF037\r\n") match {
        case message: Ping =>
          message.must(be(Ping(prefix     = None,
                               parameters = Seq("8C4EF037"))))

          message.pingValue.must(be("8C4EF037"))

        case _ => throw new Exception("Not a Ping")
      }
    }
  }

  class `Parses Mode` {
    @Test def `set by remote server` = {
       Message(":swayze MODE swayze :+i\r\n") match {
        case message: Mode =>
          message.must(be(Mode(prefix     = Option(":swayze"),
                               parameters = Seq("swayze", "+i"))))

        case _ => throw new Exception("Not a Mode")
      }
    }
  }
}
