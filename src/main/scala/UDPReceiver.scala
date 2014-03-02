import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.io.Udp
import akka.io.Udp.Received
import akka.io.Udp.Unbind
import java.net.InetSocketAddress
import akka.actor.ActorRef

class UDPReceiver(jsonHandler : ActorRef) extends Actor with ActorLogging {
    import context.system
    def receive = {
        case Received(data, from) => 
            jsonHandler ! data.utf8String
        case _ => 
    }
}
