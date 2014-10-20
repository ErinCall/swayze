package st.emily.swayze.exceptions


case class IllegalMessageException(raw: String, innerException: Exception) extends Exception
