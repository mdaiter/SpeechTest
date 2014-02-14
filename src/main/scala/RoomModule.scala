import com.tinkerpop.frames.{Property, Adjacency}
import com.tinkerpop.frames.annotations.gremlin.{GremlinGroovy, GremlinParam}
import java.lang.Iterable

trait RoomModule{

    @Property("name")
    def getName : String;

    @Property("type")
    def getType : String;

    @Property("value")
    def getValue : Int

    @GremlinGroovy("it.in('on')")
    def getAllMeasuredHormones : java.lang.Iterable[TimeVal]

    @GremlinGroovy("it.as('x').in('on').in('measured').in('contains').has('name', name).out('measured')")
    def getSpecificMeasuredHormone(@GremlinParam("name") name : String) : java.lang.Iterable[TimeVal]
}
