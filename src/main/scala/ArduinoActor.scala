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
import com.eclipsesource.json.JsonObject

class ArduinoActor(portName : String) extends Actor with akka.actor.ActorLogging{
    import context.system
    val connectionSerial = IO(Serial)
    class ArduinoReceiverActor extends Actor with akka.actor.ActorLogging{
        import context.system

        def receive = {
            case Received(data) => 
                log.info(data.utf8String)
                system.actorFor("akka://PiSystem/user/ArduinoController") ! data.utf8String
        }
    }
    val client = system.actorOf(Props( new ArduinoReceiverActor()))
    var attrToPin : ParHashMap[String, Int] = new ParHashMap[String, Int]();
    override def preStart() : Unit = {
        val port = portName 
        val baud = 9600
        val cs = 8
        val tsb = false

        val settings = SerialSettings(port, baud, cs, tsb)
        IO(Serial) ! Serial.Open(settings)
    }
    private var operator : ActorRef= null

    def receive = {
        //Add attribute to pin
        case ("set_pin",attribute : String, pinNum : Int) =>
            attrToPin += (attribute -> pinNum)
        case Opened(settings, op) => 
            operator = sender
            operator ! Register(client)
        case ("house_adjust", appliance: String, newVal : Int) =>
            log.info("Received")
            operator ! Write(genJSONSend(appliance, newVal))
        case ("house_adjust", appliances : ParVector[String], newVals : ParVector[Int]) =>
            log.info("Received multiple requests to change vals")
            operator ! Write(genJSONSend(appliances, newVals))
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

object ArduinoTest{

    def main(args: Array[String]) : Unit = {
        val system = ActorSystem("flow")
        val actor = system.actorOf(Props (new ArduinoActorSupervisor("/dev/ttyACM1")))
        val actorController = system.actorOf(Props[ArduinoControllerActorSupervisor])

        actor ! ("set_pin", "light one", 8)
        actor ! ("set_pin", "light two", 9)
        actorController ! ("light one", actor)
        actorController ! ("light two", actor)
        while(true){
            actorController ! ("house_adjust", readLine, 255)
        }
    }
}
