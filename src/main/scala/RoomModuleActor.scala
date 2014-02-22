import akka.actor.Actor
import akka.event.Logging
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener
import com.thinkaurelius.titan.core.{TitanGraph}
import com.tinkerpop.blueprints.{Edge, Vertex}

class RoomModuleActor(n : String, tModule : String, v : Int) extends TitanGraphActor with akka.actor.ActorLogging{
    protected val name : String = n
    protected val typeModule : String = tModule
    protected var value : Int = v
    protected class RoomModuleEventListener extends GraphChangedListener{
        def vertexPropertyChanged(vertex : Vertex, n : String, o : Any, o2 : Any) = {
            val nameOfVertexChanged : String = vertex.getProperty("name").asInstanceOf[String]
            val typeOfVertexChanged : String = vertex.getProperty("value").asInstanceOf[String]
            if (nameOfVertexChanged == name && typeOfVertexChanged == typeModule){
                value = vertex.getProperty("value").asInstanceOf[Int]
            }
        }
        def vertexRemoved(vertex : Vertex, map : java.util.Map[String, Object]) = {

        }
        def vertexPropertyRemoved(vertex : Vertex, s : String, o : Object) = {

        }
        def vertexAdded(vertex : Vertex) = {

        }
        def edgeAdded(edge : Edge) = {
            
        }
        def edgeRemoved(edge : Edge, map : java.util.Map[String, Object]) = {

        }
        def edgePropertyChanged(edge : Edge, key : String, setValue : Any, x : Any) = {

        }
        def edgePropertyRemoved(edge : Edge, key : String, removedValue : Any) = {

        }
    }

    override def preStart() : Unit = {
        //Do everything we did before
        super.preStart
        //Set the eventgraph to the graph
        eventGraph = new EventGraph[TitanGraph](graph)
        eventGraph.addListener(new RoomModuleEventListener())
    }
    
    override def receive = {
        case _ =>
            sender ! false
    }
}
