import akka.actor.{Actor, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.actor.ActorRef
import scala.concurrent.duration._
class UDPReceiverSupervisor(interpreter : ActorRef) extends Actor with akka.actor.ActorLogging{
    import context.system
    override val supervisorStrategy = 
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
            case _ : NullPointerException   => Restart
            case _ : Exception              => Escalate
        }
    val worker = context.actorOf(Props(new UDPReceiver(interpreter)))
    def receive = {
        case n => worker forward n
    }
}
