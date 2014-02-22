import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.{OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
//OpenCV stuff

import org.opencv.core.{Mat, Rect, MatOfRect};
class FaceRecognizerNativeSupervisor extends Actor with akka.actor.ActorLogging{
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

class FaceNativeActor(localPort : Int) extends Actor with akka.actor.ActorLogging{
    import context.system
    @native def detectFaces(mat : Mat) : MatOfRect
    override def preStart () {
        System.load("./FaceDetectGPU")
    }
    def receive = {
        case "connect" =>

    }
}

object FaceNativeTest{

    def main (args : Array[String]) {
        val system  = ActorSystem("PiSystem")
        val faceConnection = system.actorOf(Props[FaceRecognizerSupervisor])
        val udpConnection = system.actorOf(Props[UDPActorSupervisor])
        udpConnection ! "connect"
        faceConnection ! "connect"
    }
}
