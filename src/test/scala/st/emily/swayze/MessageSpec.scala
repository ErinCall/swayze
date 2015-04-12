package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class MessageToString {
    @Test def with_a_server_originated_privmsg = {
      val message = Privmsg(Option("nick!ident@host.name"),
                            Seq("target", "This is a message"))
      message.toString.must(be(":nick!ident@host.name PRIVMSG target :This is a message\r\n"))
    }

    @Test def with_a_server_originated_reply = {
      val message = Reply(Option("irc.host"),
                          Numeric.RPL_WELCOME,
                          Seq("This is a welcome message"))
      message.toString.must(be(":irc.host 001 :This is a welcome message\r\n"))
    }

    @Test def with_a_client_originated_join = {
      val message = Join(None, Seq("#channel"))
      message.toString.must(be("JOIN :#channel\r\n"))
    }

    @Test def with_a_client_originated_privmsg = {
      val message = Privmsg(None, Seq("#target", " with significant  whitespace "))
      message.toString.must(be("PRIVMSG #target : with significant  whitespace \r\n"))
    }
  }

  class ParsePrivmsg {
    @Test def with_a_normal_message = {
      Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(Option("nick!ident@host.name"),
                                  Seq("target", "This is a message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def with_a_colon_in_the_message = {
      Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(Option("nick!ident@host.name"),
                                  Seq("target", "This is a : message"))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("This is a : message"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def with_whitespace_without_truncating_it = {
      Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(Option("nick!ident@host.name"),
                                  Seq("target", "\t This is a message "))))

          message.action.must(be(false))
          message.target.must(be("target"))
          message.contents.must(be("\t This is a message "))

        case _ => throw new Exception("Not a Privmsg")
      }
    }

    @Test def with_an_action = {
      Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n") match {
        case message: Privmsg =>
          message.must(be(Privmsg(Option("nick!ident@host.name"),
                                  Seq("target", "\u0001ACTION emotes\u0001"))))

          message.action.must(be(true))
          message.target.must(be("target"))
          message.contents.must(be("emotes"))

        case _ => throw new Exception("Not a Privmsg")
      }
    }
  }

  class ParseReply {
    @Test def with_who_reply = {
      Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n") match {
        case message: Reply =>
          message.must(be(Reply(Option("irc.host"),
                                Numeric.RPL_WHOREPLY,
                                Seq("someone",
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

  class ParsePing {
    @Test def with_hex_value = {
       Message("PING :8C4EF037\r\n") match {
        case message: Ping =>
          message.must(be(Ping(None,
                               Seq("8C4EF037"))))

          message.pingValue.must(be("8C4EF037"))

        case _ => throw new Exception("Not a Ping")
      }
    }
  }

  class ParseMode {
    @Test def sent_by_remote_server = {
       Message(":swayze MODE swayze :+i\r\n") match {
        case message: Mode =>
          message.must(be(Mode(Option("swayze"),
                               Seq("swayze", "+i"))))

        case _ => throw new Exception("Not a Mode")
      }
    }
  }

  class ParseNotice {
    @Test def sent_by_remote_server = {
       Message(":irc.server NOTICE AUTH :*** Looking up your hostname...\r\n") match {
        case message: Notice =>
          message.must(be(Notice(Option("irc.server"),
                                 Seq("AUTH", "*** Looking up your hostname..."))))

        case _ => throw new Exception("Not a Notice")
      }
    }
  }
}
