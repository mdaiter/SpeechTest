import com.tinkerpop.frames.{Property, Adjacency}
import com.tinkerpop.frames.annotations.gremlin.{GremlinGroovy, GremlinParam};
import java.lang.Iterable

trait Person{
    @Property("name")
    def getName : String;
    
    @Property("type")
    def getType : String;

    @Adjacency(label="horomones")
    def getHormoneBase : HormoneBase;

    @GremlinGroovy("it.as('x').out('horomones').out('contains').has('name', name).out('measured')")
    def getHoromoneData(@GremlinParam("name") name : String) : java.lang.Iterable[TimeVal]
}
