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
        val titanGraphActor = system.actorOf(Props[TitanGraphActor], "myGraph")
        while (true){
            println(">")
            val readIn : String = readLine
            println("2>")
            val readIn2 : String = readLine
            titanGraphActor ! (readIn, readIn2)
        }
    }
}
