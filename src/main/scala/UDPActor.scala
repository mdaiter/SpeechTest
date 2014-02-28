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
class UDPReceiver extends Actor with ActorLogging {
    import context.system
    val jsonHandler = system.actorOf(Props[ChairActor], name = "ChairActor")
    def receive = {

        case Received(data, from) => 
            log.info("Received a UDP Packet {} sent from {}", data.utf8String, from)
            jsonHandler ! data.utf8String
        case _ => 
    }
}
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

object AkkaIOTest{

    def main (args : Array[String]) {
        val system  = ActorSystem("PiSystem")
        val udpInterpreter = system.actorOf(Props(new UDPReceiver))
        val udpConnection = system.actorOf(Props(new UDPActor(8008, udpInterpreter)), name = "FaceActor")
        println(udpConnection.path)
        udpConnection ! "connect"
    }
}
