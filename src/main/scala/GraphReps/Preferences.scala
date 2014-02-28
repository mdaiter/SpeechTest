import com.tinkerpop.frames.{Property, Adjacency}
import com.tinkerpop.frames.annotations.gremlin.{GremlinGroovy, GremlinParam};
import java.lang.Iterable

trait Preference{
    @Property("name")
    def getName : String;
    
    @Property("type")
    def getType : String;
    
    @GremlinGroovy("it.in('contains').in('preferences')")
    def getPeopleConnected : java.lang.Iterable[Person]
}
