import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.Actor
case class InputMap(pressure : Int, temp : Int, gyro : Int)

object InputMap {
    implicit def InputMapCodecJson : CodecJson[InputMap] = 
        casecodec3(InputMap.apply, InputMap.unapply) ("pressure", "temp", "gyro")
}

case class Chair(name : String, typeV : String, inputOptions : Option[InputMap])

object Chair{
    implicit def ChairCodecJson : CodecJson[Chair] = 
        casecodec3(Chair.apply, Chair.unapply)("name", "typeV", "inputOptions")
}

class ChairActor extends Actor {
    def receive = {
        case json : String =>
            val chair : Option[Chair] = json.decodeOption[Chair]
            
            println(chair.get.inputOptions.get.pressure)
    }
}
