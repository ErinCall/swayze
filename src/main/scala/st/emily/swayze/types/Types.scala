package st.emily.swayze.types


case class NetworkConfiguration(name:     String,
                                host:     String,
                                port:     Int,
                                channels: Seq[String],
                                modules:  Seq[String])
