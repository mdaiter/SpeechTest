import com.tinkerpop.frames.{Property, Adjacency}
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
trait HormoneBase{
    @Adjacency(label="hormone")
    def getHormones : Iterable[Hormone];
    
    @Adjacency(label="hormone")
    def addHormone(hormone : Hormone);

    @GremlinGroovy("it.as('x').out('hormone').in('hormone').except('x')")
    def getOwner : Iterable[Person]
}
