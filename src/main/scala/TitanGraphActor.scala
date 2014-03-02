import akka.actor.{Actor}
import akka.event.Logging
import com.thinkaurelius.titan.core.{TitanGraph, TitanVertex, TitanEdge, TitanFactory};
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import com.tinkerpop.blueprints.{Edge, Vertex, Graph, Direction};
import com.tinkerpop.frames.{FramedTransactionalGraph, FramedGraphFactory}
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph
import scala.collection.JavaConverters._
import scala.collection.parallel.immutable.{ParVector} 
import scala.collection.parallel.mutable.{ParHashMap}
class TitanGraphActor extends Actor with akka.actor.ActorLogging{
    protected val conf : Configuration = new BaseConfiguration();
    protected var graph : TitanGraph = null
    protected var framedGraph : FramedTransactionalGraph[TitanGraph] = null
    protected var eventGraph : EventGraph[TitanGraph] = null
    
    override def preStart(): Unit = {
        conf.setProperty("storage.backend", "cassandra")
        conf.setProperty("storage.hostname", "127.0.0.1")
        graph = TitanFactory.open(conf)

        //Make framed graph + factory
        resetFramedGraph
        eventGraph = new EventGraph(graph)
    }
    protected def resetFramedGraph = {
        val framedFactory : FramedGraphFactory = new FramedGraphFactory(new GremlinGroovyModule())
        framedGraph = framedFactory.create(graph)
    }
    def lookupPerson(name : String) : Boolean = {
        var person : Vertex = graph.getVertices("name", name).iterator.next
        if (person != null){
            return true
        }
        else{
            log.debug("Null...")
            return false
        }
    }
    
    def addWeightData (name : String, time : Long, weight : Short, instrument : String) : Boolean = {
        var person : Vertex = graph.getVertices("name", name).iterator.next
        if (person != null){
            var weightPerson : Vertex = person.query().labels("weight").vertices.iterator.next
            //create a vertex and set the properties
            var newWeight : Vertex = graph.addVertex(null)
            newWeight.setProperty("value", weight)
            newWeight.setProperty("time", time)
            //link the two vertices
            graph.addEdge(null, weightPerson, newWeight, "weighed")
            //link the newWeight to the instrument
            var instrumentVertex : Vertex = graph.getVertices("name", instrument).iterator.next
            graph.addEdge(null, newWeight, instrumentVertex, "on")
            return true
        }
        else{
            return false
        }
    }

    def addHormonalData(name : String, horomone : String, time : Long, level : Float, instrument : String) : Boolean = {
        var person : Vertex = graph.getVertices("name", name).iterator.next
        if (person != null){
            var horomonesPerson : Vertex = person.query().labels("horomones").vertices().iterator().next()
            var newHoromone : Vertex = graph.addVertex(null)
            newHoromone.setProperty("time", time)
            newHoromone.setProperty("level", level)
            graph.addEdge(null, horomonesPerson, newHoromone, "contains")
            
            var instrumentVertex : Vertex = graph.getVertices("name", instrument).iterator.next
            graph.addEdge(null, newHoromone, instrumentVertex, "at")
            graph.commit
            return true;
        }
        else{
            return false
        }
    }
    
    def lookupPersonGroovy(name : String) : Boolean = {
        val person : Person = framedGraph.getVertices("name", name, classOf[Person]).iterator.next
        println(person.getName)
        val timeVals : Iterable[TimeVal] = person.getHoromoneData("noradphedamine").asScala
        timeVals.map(x => println(x.getValue))
        return true
    }

    def modifyPersonProperty(name : String, property : String, updateVal : Object) : Boolean = {
        var person : Vertex = graph.getVertices("name", name).iterator.next
        if (person != null){
            person.setProperty(property, updateVal)
            graph.commit
            return true
        }
        else{
            log.debug("Null lookup\nAborting")
            return false
        }
    }
    def setPrefs(names : ParVector[String], furnitureToVal : ParHashMap[String, Int]) = {
        log.info("Setting prefs")
        for (name <- names){
            for (tupFurToVal <- furnitureToVal){
                log.info("Setting value ${tupFurToVal._1} tp val ${tupFurToVal._2}")
                var personPrefSpecific = graph.getVertices("name", name++"Preferences"++tupFurToVal._1).iterator.next   
                personPrefSpecific.setProperty("value", tupFurToVal._2)
                graph.commit()
            }
        }
    }

    def getPrefs(name : String) : (String, ParVector[String], ParVector[Int])= {
        log.info("Getting prefs for " ++ name)
        var personMap : ParHashMap[String, Int] = new ParHashMap[String, Int]
        val personPrefs : Vertex = graph.getVertices("name", name++"Preferences").iterator.next
        log.info(personPrefs.toString)
        for (vertex <- personPrefs.getVertices(Direction.OUT, "prefers").iterator.asScala){
            val tempInt : Int = vertex.getProperty("value").asInstanceOf[Int]
            val tempName : String = vertex.getProperty("name").asInstanceOf[String]
            personMap += (tempName -> tempInt )
        }
        val personStrings : ParVector[String] = personMap.keys.toVector.par
        val personVals : ParVector[Int] = personMap.values.toVector.par
        log.info("Result from getPrefs: " ++ personVals.toString)
        return ("house_adjust", personStrings, personVals)
    }
    def addPerson(name : String) = {
        val newPerson = graph.addVertex(null)
        newPerson.setProperty("name", name)
        //setup nodes for person to link to
        var personHormoneNode = graph.addVertex(null)
        var personWeightNode = graph.addVertex(null)
        val personPreferencesNode = graph.addVertex(null)
        personHormoneNode.setProperty("name", name++"Horomones")
        personWeightNode.setProperty("name", name++"Weight")
        personPreferencesNode.setProperty("name", name++"Preferences")
        graph.addEdge(null, newPerson, personHormoneNode, "horomones")
        graph.addEdge(null, newPerson, personWeightNode, "weight")
        graph.addEdge(null, newPerson, personPreferencesNode, "preferences")
        graph.commit()
    }

    def receive = {
        case ("lookupPerson", i : String) =>
            sender ! lookupPerson(i)
        case ("addPerson", i : String) =>
            addPerson(i)
        case ("lookupPersonGroovy", i : String) =>
            lookupPersonGroovy(i)
        case ("addHormonalData", name : String, horomone : String, time : Long, level : Float, instrument : String) =>
            sender ! addHormonalData(name, horomone, time, level, instrument)
        case ("set_prefs", names : Any, nameToVal : Any) =>
            setPrefs(names.asInstanceOf[ParVector[String]], nameToVal.asInstanceOf[ParHashMap[String, Int]])
        case ("get_prefs", name: String) =>
            sender ! getPrefs(name)
        case _ =>
            log.info("We're being attacked!!!")
    }
}
