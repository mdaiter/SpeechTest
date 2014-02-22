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
class UDPActorSupervisor extends Actor with akka.actor.ActorLogging{
    import context.system
    override val supervisorStrategy = 
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
            case _ : NullPointerException   => Restart
            case _ : Exception              => Escalate
        }
    val worker = context.actorOf(Props(new UDPActor(8002)))
    def receive = {

        case n => worker forward n
    }
}
