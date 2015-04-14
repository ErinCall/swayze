package st.emily.swayze.irc

import com.simple.simplespec.Spec
import org.junit.Test

import Command._
import Numeric._


class MessageSpec extends Spec {
  class MessageToStringCanPrint {
    @Test def a_server_originated_privmsg = {
      new Message(prefix     = Option("nick!ident@host.name"),
                  command    = Option(PRIVMSG),
                  numeric    = None,
                  parameters = Seq("target", "This is a message"))
        .toString.must(be(":nick!ident@host.name PRIVMSG target :This is a message\r\n"))
    }

    @Test def a_server_originated_reply = {
      new Message(prefix     = Option("irc.host"),
                  command    = None,
                  numeric    = Option(RPL_WELCOME),
                  parameters = Seq("This is a welcome message"))
        .toString.must(be(":irc.host 001 :This is a welcome message\r\n"))
    }

    @Test def a_client_originated_join = {
      new Message(prefix     = None,
                  command    = Option(JOIN),
                  numeric    = None,
                  parameters = Seq("#channel"))
        .toString.must(be("JOIN :#channel\r\n"))
    }

    @Test def a_client_command_without_parameters = {
      new Message(prefix     = None,
                  command    = Option(QUIT),
                  numeric    = None,
                  parameters = Seq())
        .toString.must(be("QUIT\r\n"))
    }

    @Test def a_client_originated_privmsg_with_significant_whitespace = {
      new Message(prefix     = None,
                  command    = Option(PRIVMSG),
                  numeric    = None,
                  parameters = Seq("#target", " with significant  whitespace ")).toString
        .must(be("PRIVMSG #target : with significant  whitespace \r\n"))
    }
  }

  class MessageFromStringCanParse {
    @Test def a_normal_privmsg = {
      Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")
        .must(be(new Message(prefix     = Option("nick!ident@host.name"),
                             command    = Option(PRIVMSG),
                             numeric    = None,
                             parameters = Seq("target", "This is a message"))))
    }

    @Test def a_privmsg_containing_a_colon = {
      Message(":nick!ident@host.name PRIVMSG target :This is a : message\r\n")
        .must(be(new Message(prefix     = Option("nick!ident@host.name"),
                             command    = Option(PRIVMSG),
                             numeric    = None,
                             parameters = Seq("target", "This is a : message"))))
    }

    @Test def a_privmsg_containing_significant_whitespace = {
      Message(":nick!ident@host.name PRIVMSG target :\t This is a message \r\n")
        .must(be(new Message(prefix     = Option("nick!ident@host.name"),
                             command    = Option(PRIVMSG),
                             numeric    = None,
                             parameters = Seq("target", "\t This is a message "))))
    }

    @Test def a_privmsg_containing_an_action = {
      Message(":nick!ident@host.name PRIVMSG target :\u0001ACTION emotes\u0001\r\n")
        .must(be(new Message(prefix     = Option("nick!ident@host.name"),
                             command    = Option(PRIVMSG),
                             numeric    = None,
                             parameters = Seq("target", "\u0001ACTION emotes\u0001"))))
    }

    @Test def a_who_reply = {
      Message(":irc.host 352 someone #channel user 0.0.0.0 irc.host someone G :0 Real Name\r\n")
        .must(be(new Message(prefix     = Option("irc.host"),
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

    @Test def a_ping_with_hex_value = {
      Message("PING :8C4EF037\r\n")
        .must(be(new Message(prefix     = None,
                             command    = Option(PING),
                             numeric    = None,
                             parameters = Seq("8C4EF037"))))
    }

    @Test def a_mode_from_the_remote_server = {
      Message(":swayze MODE swayze :+i\r\n")
        .must(be(new Message(prefix     = Option("swayze"),
                             command    = Option(MODE),
                             numeric    = None,
                             parameters = Seq("swayze", "+i"))))
    }

    @Test def a_notice_sent_by_remote_server = {
      Message(":irc.server NOTICE AUTH :*** Looking up your hostname...\r\n")
        .must(be(new Message(prefix     = Option("irc.server"),
                             command    = Option(NOTICE),
                             numeric    = None,
                             parameters = Seq("AUTH", "*** Looking up your hostname..."))))
    }

    @Test def a_quit_sent_by_the_local_client = {
      Message("QUIT\r\n")
        .must(be(new Message(prefix     = None,
                             command    = Option(QUIT),
                             numeric    = None,
                             parameters = Seq())))
    }
  }
}
