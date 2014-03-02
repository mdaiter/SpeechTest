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
import scala.collection.parallel.mutable.{ParHashMap}
import scala.collection.parallel.immutable.ParVector
import com.eclipsesource.json.{JsonObject, JsonArray}
import scala.collection.JavaConverters._
class ArduinoActor(portName : String) extends Actor with akka.actor.ActorLogging{
    import context.system
    val connectionSerial = IO(Serial)
    class ArduinoReceiverActor extends Actor with akka.actor.ActorLogging{
        import context.system
        private var jsonString = new String();
        private var counter = 0;
        def receive = {
            case Received(data) => 
                log.info(data.utf8String)
                //jsonString += data.utf8String
                if (data.utf8String.contains('}')){
                    jsonString += data.utf8String.substring(0, data.utf8String.indexOf('}')+1)
                    log.info("Hit here. jsonString = " ++ jsonString)
                    val jsonObj : JsonObject = JsonObject.readFrom(jsonString)
                    log.info("Parsed json")
                    val namesArr : ParVector[String] =  jsonObj.get("names").asArray.values.asScala.map(x => x.asString).toVector.par
                    log.info("Parsed arr")
                    val jsonObjRmvd = jsonObj.remove("names").asObject
                    var tmpJson : ParHashMap[String, Int] = new ParHashMap[String, Int]();
                    for (pinObj <- jsonObjRmvd.iterator.asScala){
                        tmpJson += (pinToAttr.get(pinObj.getName.toInt).get -> pinObj.getValue.asInt)
                    }
                    log.info("Setting these people: " ++ namesArr.toString ++ " as this: " ++ tmpJson.toString)
                    system.actorFor("akka://mySystem/user/TitanActor") ! ("set_prefs", namesArr, tmpJson)
                    jsonString = ""
                }
                else{
                    jsonString += data.utf8String
                }
        }
    }
    val client = system.actorOf(Props( new ArduinoReceiverActor()))
    var attrToPin : ParHashMap[String, Int] = new ParHashMap[String, Int]();
    var pinToAttr : ParHashMap[Int, String] = new ParHashMap[Int, String]();
    override def preStart() : Unit = {
        val port = portName 
        val baud : Int = 9600
        val cs : Int  = 8
        val tsb : Boolean = false

        val settings = SerialSettings(port, baud, cs, tsb)
        IO(Serial) ! Serial.Open(settings)
    }
    private var operator : ActorRef= null

    def receive = {
        //Add attribute to pin
        case ("set_pin",attribute : String, pinNum : Int) =>
            attrToPin += (attribute -> pinNum)
            pinToAttr += (pinNum -> attribute)
        case Opened(settings, op) => 
            operator = sender
            operator ! Register(client)
        case ("house_adjust", appliance: String, newVal : Int) =>
            log.info("Received")
            operator ! Write(genJSONSend(appliance, newVal))
        case ("house_adjust", appliances : ParVector[String], newVals : ParVector[Int]) =>
            log.info("Received multiple requests to change vals")
            operator ! Write(genJSONSend(appliances, newVals))
        case ("set_prefs", names : ParVector[String]) =>
            log.info("Received request to modify settings for these people: " ++ names.toString)
            val namesJsonArray = new JsonArray()
            names.map(x => namesJsonArray.add(x))
            operator ! Write(ByteString.fromString((new JsonObject().add("get","get").add("names", namesJsonArray)).toString))
    }
    def genJSONSend(attribute : String, newVal : Int) : ByteString = {
        val pinNum : Int = attrToPin.get(attribute).get
        val jsonPinObject : JsonObject = new JsonObject().add(pinNum.toString, newVal)
        val jsonObject : JsonObject = new JsonObject().add("pwm", jsonPinObject)
        ByteString.fromString(jsonObject.toString())
    }
    def genJSONSend(attributes : ParVector[String], newVals : ParVector[Int]) : ByteString = {
        val pinNums : ParVector[Int] = attributes.map(x => attrToPin.get(x).get)
        var jsonPinObjects : JsonObject = new JsonObject()
        (pinNums zip newVals).map{case (p, n) => jsonPinObjects.add(p.toString, n)}
        val jsonObject : JsonObject = new JsonObject().add("pwm", jsonPinObjects)
        ByteString.fromString(jsonObject.toString())
    }
}
