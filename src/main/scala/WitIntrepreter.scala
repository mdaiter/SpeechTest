import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.{ActorLogging, Actor}
import com.eclipsesource.json.JsonObject;
import akka.util.ByteString
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Await
import scala.collection.parallel.immutable.ParVector
class WitJSONActor extends Actor  with akka.actor.ActorLogging{
    import context.system
    def receive = {
        case json : String =>
            sender ! analyze(json)
    }

    def analyze(json : String) = {
        val jsonObject : JsonObject = JsonObject.readFrom(json);
        val intent : String = jsonObject.get("outcome").asObject().get("intent").asString;
        val entities = jsonObject.get("outcome").asObject().get("entities").asObject;
        println(intent);
        println(entities);
        intent match {
            case "house_adjust" => analyzeHouseAdjust(entities)
            case "get_prefs" => analyzePrefsGet(entities)
            case "set_prefs" => analyzePrefsSet(entities)
        }
    }

    def analyzeHouseAdjust(entities : JsonObject) = {
        val isOnOrOff = if (entities.get("on_off").asObject().get("value").asString() == "on"){
                255
            } else {
                0
            };
        

        val appliance = if (entities.get("appliances").asObject().get("value").asString() != null){
            entities.get("appliances").asObject().get("value").asString
        } else {null}
        println(appliance)
        
        ("house_adjust", appliance, isOnOrOff )
    }
    def analyzePrefsGet(entities : JsonObject) = {
        val name = if (entities.get("contact").asObject.get("value").asString != null){
            entities.get("contact").asObject.get("value").asString.head.toUpper.toString ++ entities.get("contact").asObject.get("value").asString.tail
        } else{null}
        implicit val timeout = Timeout(3 seconds)
        val futureGetPrefs = system.actorFor("akka://mySystem/user/TitanActor") ? ("get_prefs", name)
        val prefs = Await.result(futureGetPrefs, timeout.duration)
        log.info("Received prefs" ++ prefs.toString)
        prefs
    }
    def analyzePrefsSet(entities : JsonObject) = {
        /*val appliance = if (entities.get("appliances").asObject().get("value").asString() != null){
            entities.get("appliances").asObject().get("value").asString
        } else {null}

        val value : Int = entities.get("level").asObject().get("value").asInt
        */
        /*implicit val timeout = Timeout(3 seconds)
        val futureGetName = system.actorFor("akka://mySystem/user/FaceActor") ? "getNames"
        val faces = Await.result(futureGetName, timeout.duration)*/

       val face : ParVector[String] = new ParVector[String] :+ entities.get("contact").asObject.get("value").asString
        
       ("set_prefs", face)
    }
}
