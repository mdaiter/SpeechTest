import com.tinkerpop.frames.{Property, Adjacency}

trait TimeVal{
    @Property("time")
    def getTime : Long;
    
    @Property("value")
    def getValue : Int;

//    @Adjacency(label="on")
//    def getInstrument : Iterable[Instrument]
}
