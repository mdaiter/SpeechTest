import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.Actor

case class InputFaceMap(name : String, x: Int, y : Int)

object InputFaceMap {
    implicit def InputFaceMapCodecJson : CodecJson[InputFaceMap] = 
        casecodec3(InputFaceMap.apply, InputFaceMap.unapply) ("name", "positionX", "positionY")
}

case class Face(name : String, typeV : String, inputOptions : Array[Option[InputFaceMap]])

object Face{
    implicit def FaceCodecJson : CodecJson[Face] = 
        casecodec3(Face.apply, Face.unapply)("name", "type", "inputOptions")
}

class FaceJsonActor extends Actor {
    def receive = {
        case ("inBounds", json : String, x : Int, y : Int, width : Int, height : Int) =>
            val face : Option[Face] = json.decodeOption[Face]
            
            println(face.get.inputOptions)
    }
}
