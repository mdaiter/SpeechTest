import akka.actor.{Actor}
import akka.event.Logging
import com.thinkaurelius.titan.core.{TitanGraph, TitanVertex, TitanEdge, TitanFactory};
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import com.tinkerpop.blueprints.{Edge, Vertex, Graph};
import com.tinkerpop.frames.{FramedTransactionalGraph, FramedGraphFactory}

/*
class PersonActor(name : String) extends TitanGraphActor {
    def getName : String = {
        return name
    }
    
    def getHormoneBase () : Vertex = {
        val person : Vertex = graph.getVertices("name", name).iterator.next
        val hormoneBasePerson :Vertex = person.query.labels("horomones").vertices.iterator.next
        return hormoneBasePerson
    }
    
    def getHormoneSpecific(name : String) : Vertex = {
        val hormoneBase = this.getHormoneBase
        val hormoneSpecific = hormoneBase.query.labels(name).vertices.iterator.next
        return hormoneSpecific
    }

    def getHormoneSpecificData(name : String, start : Long, end : Long) : Iterator[Vertex] = {
        val hormoneData : Iterator[Vertex] = this.getHormoneSpecific(name).query.labels("contain").vertices
        val filteredData : Iterator[Vertex] = hormoneData.filter(x =>
                x.getProperty("time") > start && x.getProperty("time") < end
                );
        val filteredDataValues : List[Short] = filteredData.map(x => x.getProperty("value"));
        return filteredDataValues
    }

    def receive = {
        
    }
    
}*/

class PersonActor extends TitanGraphActor {
    
}
