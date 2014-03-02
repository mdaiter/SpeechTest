import akka.actor.{Actor, ActorLogging ,ActorRef, ActorSystem, Props}
import com.eclipsesource.json.{JsonObject, JsonArray}
import scala.collection.parallel.immutable.ParVector
import scala.collection.JavaConverters._
class FaceActor extends Actor with akka.actor.ActorLogging{
    import context.system
    private var lastNamesRecv : ParVector[String] = new ParVector[String];
    def receive = {
        case "getNames" =>
            sender ! lastNamesRecv
        case json : String =>
            analyze(json)
    }
    def analyze(json : String) = {
        val jsonObj : JsonObject = JsonObject.readFrom(json)
        val namesArr : JsonArray = jsonObj.get("faces").asArray
        val names : ParVector[String] = namesArr.values.asScala.map(x => x.asObject.get("name").asString).toVector.par
        //log.info("Received face vector: " ++ names.toString)
        lastNamesRecv = names
    }
}
