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
            arduinoSessions.get(attribute).get ! ("house_adjust", attribute, output)
        case ("house_adjust", attributes : ParVector[String], outputs : ParVector[Int]) =>
            attributes.map(x => arduinoSessions.get(x).get ! ("house_adjust", attributes, outputs))
    }
}
