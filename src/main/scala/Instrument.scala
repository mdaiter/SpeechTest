import com.tinkerpop.frames.{Property, Adjacency}

trait Instrument{
    @Property("name")
    def getName : String;
    
    @Property("type")
    def getType : String;

}
