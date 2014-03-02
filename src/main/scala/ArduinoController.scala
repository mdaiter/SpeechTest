import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorLogging
import com.github.jodersky.flow.Serial._
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.ByteString
import akka.io.IO
import com.github.jodersky.flow.Serial
import com.github.jodersky.flow.SerialSettings
import akka.actor.ActorRef
import scala.collection.parallel.mutable.ParHashMap
import scala.collection.parallel.immutable.ParVector

class ArduinoController extends Actor with akka.actor.ActorLogging{
    import context.system
    var arduinoSessions = new ParHashMap[String, ActorRef]
    
    def receive = {
        //Add new arduino actor with attribute (light1, etc.)
        case (attribute : String, newArduino : ActorRef) =>
            arduinoSessions += (attribute -> newArduino)
        case (attributes : ParVector[String], newArduino : ActorRef) =>
            attributes.map( x => arduinoSessions += (x -> newArduino))
        case ("house_adjust", attribute : String, output : Int) =>
            log.info("Adjusting a few things...")
            arduinoSessions.get(attribute).get ! ("house_adjust", attribute, output)
        case ("house_adjust", attributes : Any, outputs : Any) =>
            (attributes.asInstanceOf[ParVector[String]] zip outputs.asInstanceOf[ParVector[Int]]).map{
                case (attribute, output) =>
                    arduinoSessions.get(attribute).get ! ("house_adjust", attribute, output)
                    log.info(attribute ++ output.toString)
            }
        case ("set_prefs", names : ParVector[String]) =>
            arduinoSessions.map(x => arduinoSessions.get(x._1).get ! ("set_prefs", names))
    }
}
