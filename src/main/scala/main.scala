import akka.actor.{Actor, ActorSystem, Props}
import com.thinkaurelius.titan.core.{TitanGraph, TitanVertex, TitanEdge, TitanFactory};
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import com.tinkerpop.blueprints.{Edge, Vertex, Graph};
import com.tinkerpop.frames.FramedGraph


class TitanActor extends Actor {

    def receive = {
        case ("query", s : String) =>
            sender ! "test"
        case _ =>
            sender ! false
    }
}

object Main {
    def main(args : Array[String]){
        val system = ActorSystem("mySystem")
        val micActor = system.actorOf(Props[SpeechActorSupervisor], "Microphone")
        val titanGraphActor = system.actorOf(Props[TitanGraphActor], "TitanActor")
        val witActor = system.actorOf(Props[WitAIActor], "WitAIActor")
        val faceActor = system.actorOf(Props[FaceActorSupervisor], "FaceActor")
        val udpReceiverActor = system.actorOf(Props(new UDPReceiverSupervisor(faceActor)))
        val udpActor = system.actorOf(Props(new UDPActorSupervisor(8008, udpReceiverActor)))
        val arduinoControllerActor = system.actorOf(Props[ArduinoControllerActorSupervisor], name="ArduinoController")
        val arduinoActor = system.actorOf(Props (new ArduinoActorSupervisor("/dev/ttyACM0")))
        val synthActor = system.actorOf(Props[SpeechSynthActor], "SynthActor")
        witActor ! micActor
        
        udpActor ! "connect"

        arduinoActor ! ("set_pin", "light one", 8)
        arduinoActor ! ("set_pin", "light two", 9)
        //arduinoActor ! ("set_pin", "MattPreferenceslight one", 8)
        //arduinoActor ! ("set_pin", "MattPreferenceslight two", 9)

        arduinoControllerActor ! ("light one", arduinoActor)
        arduinoControllerActor ! ("light two", arduinoActor)
        //arduinoControllerActor ! ("MattPreferenceslight one", arduinoActor)
        //arduinoControllerActor ! ("MattPreferenceslight two", arduinoActor)
        while (true){
            print(">")
            witActor ! readLine

        }
    }
}
