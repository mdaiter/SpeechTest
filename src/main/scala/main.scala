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
        val micActor = system.actorOf(Props[SpeechActor], "Microphone")
        val titanGraphActor = system.actorOf(Props[TitanGraphActor], "myGraph")
        val witActor = system.actorOf(Props[WitAIActor], "WitAIActor")

        val arduinoControllerActor = system.actorOf(Props[ArduinoControllerActorSupervisor], name="ArduinoController")
        val arduinoActor = system.actorOf(Props (new ArduinoActorSupervisor("/dev/ttyACM1")))
        
        witActor ! micActor
        
        arduinoActor ! ("set_pin", "light one", 8)
        arduinoActor ! ("set_pin", "light two", 9)

        arduinoControllerActor ! ("light one", arduinoActor)
        arduinoControllerActor ! ("light two", arduinoActor)
        while (true){
            print(">")
            witActor ! readLine

        }
    }
}
