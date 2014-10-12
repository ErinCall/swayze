package st.emily.swayze.irc

import Command.Command
import Numeric.Numeric

import com.simple.simplespec.Spec
import org.junit.Test


class MessageSpec extends Spec {
  class `MessageParser` {
    @Test def `Parses PRIVMSG` = {
      Message(":nick!ident@host.name PRIVMSG target :This is a message\r\n")
        .must(be(Privmsg(raw        = ":nick!ident@host.name PRIVMSG target :This is a message\r\n",
                         prefix     = Option(":nick!ident@host.name"),
                         command    = Command.PRIVMSG,
                         parameters = Seq("target", "This is a message"),
                         action     = false,
                         target     = "target")))
    }
  }
}
