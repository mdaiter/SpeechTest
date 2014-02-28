import com.tinkerpop.frames.{Property, Adjacency}

trait Hormone{
    @Property("name")
    def getName : String;
    
    @Property("type")
    def getType : String;

    @Adjacency(label="measured")
    def getTimeVals : Iterable[TimeVal]
}
