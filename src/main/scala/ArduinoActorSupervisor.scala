import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.{OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.io.IO
import akka.actor.ActorRef
import scala.concurrent.duration._
class ArduinoActorSupervisor(name : String) extends Actor with akka.actor.ActorLogging{
    import context.system
    override val supervisorStrategy = 
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
            case _ : NullPointerException   => Restart
            case _ : Exception              => Escalate
        }
    val worker = context.actorOf(Props(new ArduinoActor(name)))
    def receive = {
        case n => worker forward n
    }
}
