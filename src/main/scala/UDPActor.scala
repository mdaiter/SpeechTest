import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.io.Udp
import akka.io.Udp.Bind
import akka.io.Udp.Bound
import akka.io.Udp.Received
import akka.io.Udp.Unbind
import java.net.InetSocketAddress
import akka.actor.ActorRef

class UDPActor(localPort : Int, handler : ActorRef) extends Actor with akka.actor.ActorLogging{
    import context.system
    val connectionlessUdp = IO(Udp)
    def receive = {
        case "connect" =>
            IO(Udp) ! Bind(handler, new InetSocketAddress(localPort))
        case Bound =>
            val worker = sender
            context.become {
                case "disconnect" =>
                    worker ! Unbind
                    context.become(receive)
            }
    }
}
