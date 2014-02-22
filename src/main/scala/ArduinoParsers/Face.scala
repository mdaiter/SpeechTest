import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.Actor

case class InputFaceMap(x: Int, y : Int)

object InputFaceMap {
    implicit def InputFaceMapCodecJson : CodecJson[InputFaceMap] = 
        casecodec2(InputFaceMap.apply, InputFaceMap.unapply) ("X", "Y")
}

case class Face(name : String, typeV : String, inputOptions : Option[InputFaceMap])

object Face{
    implicit def FaceCodecJson : CodecJson[Face] = 
        casecodec3(Face.apply, Face.unapply)("name", "typeV", "inputOptions")
}

class FaceJsonActor extends Actor {
    def receive = {
        case ("inBounds", json : String, x : Int, y : Int, width : Int, height : Int) =>
            val face : Option[Face] = json.decodeOption[Face]
            
            println(face.get.inputOptions.get.x)
            if (face.get.inputOptions.get.x > x && face.get.inputOptions.get.y > y &&
                face.get.inputOptions.get.x < x + width && face.get.inputOptions.get.y < y + height) {
                sender ! true
            }
            else {
                sender ! false
            }
    }
}
