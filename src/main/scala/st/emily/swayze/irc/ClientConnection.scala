package st.emily.swayze.irc

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress


object ClientConnection {
  def props(remote: InetSocketAddress, service: ActorRef) =
    Props(classOf[ClientConnection], remote, service)
}


/**
 * Maintains the network connection to an IRC server. Heavily adapted
 * from the EchoManager/EchoHandler sample included with Akka.
 *
 * Provides the TCP connection to an IRC server on behalf of its daemon
 * supervisor. Delegates IRC events to a client service for handling.
 *
 * @param remote the server's info for connecting (a host and port)
 * @param service client service for handling IRC events
 */
class ClientConnection(remote: InetSocketAddress, service: ActorRef) extends Actor with ActorLogging {
  import Tcp._
  import context.system

  context.watch(self)

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def preStart: Unit = IO(Tcp) ! Connect(remote)

  override def postRestart(thr: Throwable): Unit = context.stop(self)

  override def postStop: Unit = log.info(s"Transferred $transferred bytes from/to [$remote]")

  case class Ack(length: Int) extends Event

  override def receive: Receive = {
    case Connected(remote, local) =>
      log.info(s"Connected to $remote")
      sender() ! Register(self, keepOpenOnPeerClosed = true)
    case Received(data) =>
      self ! Ack(currentOffset)
      buffer(data)
    case Ack(ack) =>
      acknowledge(ack)
    case PeerClosed =>
      if (storage.isEmpty) {
        context.stop(self)
      } else {
        context.become(closing)
      }
  }

  def closing: Receive = {
    case CommandFailed(_: Write) =>
      context.become({
        case ack: Int =>
          acknowledge(ack)
      }, discardOld = false)

    case Ack(ack) =>
      acknowledge(ack)
      if (storage.isEmpty) {
        context.stop(self)
      }
  }

  private[this] var storageOffset = 0
  private[this] var storage       = Vector.empty[ByteString]
  private[this] var stored        = 0L
  private[this] var transferred   = 0L
  private[this] val maxStored     = 100000000L
  private[this] val highWatermark = maxStored * 5 / 10
  private[this] val lowWatermark  = maxStored * 3 / 10
  private[this] var suspended     = false

  private[this] def currentOffset = storageOffset + storage.size

  private[this] def buffer(data: ByteString): Unit = {
    log.debug(s"Buffering: ${data.utf8String.trim}")

    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      log.warning(s"Buffer overrun while connected to $remote")
      context stop self
    } else if (stored > highWatermark) {
      log.debug(s"Suspending reading at $currentOffset")
      self ! SuspendReading
      suspended = true
    }
  }

  private[this] def acknowledge(ack: Int): Unit = {
    require(ack == storageOffset, s"received ack $ack at $storageOffset")
    require(storage.nonEmpty, s"storage was empty at ack $ack")

    val size = storage(0).size
    stored -= size
    transferred += size

    storageOffset += 1
    storage = storage.drop(1)

    if (suspended && stored < lowWatermark) {
      log.debug("Resuming reading")
      self ! ResumeReading
      suspended = false
    }

    // TODO: do stuff here
  }
}
