package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class `Parsing tests` {
    @Test def `Parses PRIVMSG` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :This is a message\r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "This is a message"),
                              numeric    = None)))

      message.action.must(be(false))
      message.target.must(be(Option("target")))
      message.contents.must(be(Option("This is a message")))
    }

    @Test def `Parses PRIVMSG with a colon in the message` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :This is a : message\r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "This is a : message"),
                              numeric    = None)))

      message.action.must(be(false))
      message.target.must(be(Option("target")))
      message.contents.must(be(Option("This is a : message")))
    }

    @Test def `Parses PRIVMSG keeping whitespace` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "\t This is a message "),
                              numeric    = None)))

      message.action.must(be(false))
      message.target.must(be(Option("target")))
      message.contents.must(be(Option("\t This is a message ")))
    }

    @Test def `Parses PRIVMSG which contains an action` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "\u0001ACTION emotes\u0001"),
                              numeric    = None)))

      message.action.must(be(true))
      message.target.must(be(Option("target")))
      message.contents.must(be(Option("emotes")))
    }

    @Test def `Parses REPLY` = {
      val message = Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n")

      message.must(be(Message(raw        = Option(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n"),
                              prefix     = Option(":irc.host"),
                              command    = None,
                              parameters = Seq("someone", "#channel", "user", "0.0.0.0", "irc.host", "someone", "G", "0 Real Name"),
                              numeric    = Option(Numeric.withName("352")))))
    }

    @Test def `Parses PING` = {
      val message = Message("PING :8C4EF037\r\n")

      message.must(be(Message(raw        = Option("PING :8C4EF037\r\n"),
                              prefix     = None,
                              command    = Option(Command.PING),
                              parameters = Seq("8C4EF037"),
                              numeric    = None)))

      message.pingValue.must(be(Option("8C4EF037")))
    }
  }

  class `Construction tests` {
    @Test def `Creates legal PONG message` = {
      val message = Message(command    = Option(Command.PONG),
                            parameters = Seq("8C4EF037"))

      message.must(be(Message(raw        = None,
                              prefix     = None,
                              command    = Option(Command.PONG),
                              parameters = Seq("8C4EF037"),
                              numeric    = None)))

      message.toRawMessageString.must(be("PONG :8C4EF037\r\n"))
    }
  }
}
