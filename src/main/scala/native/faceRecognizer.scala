import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.{OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.io.IO
import akka.io.Udp
import akka.io.Udp.Bind
import akka.io.Udp.Bound
import akka.io.Udp.Received
import akka.io.Udp.Unbind
import java.net.InetSocketAddress
import scala.concurrent.duration._

class FaceRecognizerSupervisor extends Actor with akka.actor.ActorLogging{
    import context.system
    override val supervisorStrategy = 
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
            case _ : NullPointerException   => Restart
            case _ : Exception              => Escalate
        }
    val worker = context.actorOf(Props(new FaceUDPActor(8003)))
    def receive = {

        case n => worker forward n
    }
}

class FaceReceiver extends Actor with ActorLogging {
    import context.system
    val jsonHandler = system.actorOf(Props[FaceJsonActor], name = "FaceActor")
    val chairX = 300
    val chairWidth = 50
    val chairY = 300
    val chairHeight = 50
    def receive = {
        case Received(data, from) => 
            log.info("Received a UDP Packet {} sent from {}", data.utf8String, from)
            jsonHandler ! ("inBounds", data.utf8String, chairX, chairY, chairWidth, chairHeight)
        case _ => 
    }
}
class FaceUDPActor(localPort : Int) extends Actor with akka.actor.ActorLogging{
    import context.system
    val connectionlessUdp = IO(Udp)
    val handler = system.actorOf(Props[FaceReceiver])
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

object FaceTest{

    def main (args : Array[String]) {
        val system  = ActorSystem("PiSystem")
        val faceConnection = system.actorOf(Props[FaceRecognizerSupervisor])
        val udpConnection = system.actorOf(Props[UDPActorSupervisor])
        udpConnection ! "connect"
        faceConnection ! "connect"
    }
}
