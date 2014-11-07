package st.emily.swayze.exceptions


case class FailedParseException(message: String, inner: Exception)
extends Exception(s"$message (${inner.getClass}: ${inner.getMessage})")
