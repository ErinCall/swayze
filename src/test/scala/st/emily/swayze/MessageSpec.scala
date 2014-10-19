package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class `MessageParser` {
    @Test def `Parses PRIVMSG` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :This is a message\r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "This is a message"),
                              numeric    = None)))

      message.action.must(be(false))
      message.target.must(be("target"))
      message.contents.must(be("This is a message"))
    }

    @Test def `Parses PRIVMSG with a colon in the message` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :This is a : message\r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "This is a : message"),
                              numeric    = None)))

      message.action.must(be(false))
      message.target.must(be("target"))
      message.contents.must(be("This is a : message"))
    }

    @Test def `Parses PRIVMSG keeping whitespace` = {
      val message = Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n")

      message.must(be(Message(raw        = Option(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n"),
                              prefix     = Option(":nick!ident@host.name"),
                              command    = Option(Command.PRIVMSG),
                              parameters = Seq("target", "\t This is a message "),
                              numeric    = None)))
      message.action.must(be(false))
      message.target.must(be("target"))
      message.contents.must(be("\t This is a message "))
    }

    @Test def `Parses REPLY` = {
      val message = Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n")

      message.must(be(Message(raw        = Option(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n"),
                              prefix     = Option(":irc.host"),
                              command    = None,
                              parameters = Seq("someone", "#channel", "user", "0.0.0.0", "irc.host", "someone", "G", "0 Real Name"),
                              numeric    = Option(Numeric.withName("352")))))
    }
  }
}
