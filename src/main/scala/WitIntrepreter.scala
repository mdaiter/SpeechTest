import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.{ActorLogging, Actor}
import com.eclipsesource.json.JsonObject;
import akka.util.ByteString
class WitJSONActor extends Actor  with akka.actor.ActorLogging{
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

    def analyzePrefsSet(entities : JsonObject) = {
        val appliance = if (entities.get("appliances").asObject().get("value").asString() != null){
            entities.get("appliances").asObject().get("value").asString
        } else {null}

        val value : Int = entities.get("level").asObject().get("value").asInt

        ("set_prefs", value, appliance)
    }
}
