import argonaut._, Argonaut._
import scalaz._, Scalaz._
import akka.actor.Actor
case class InputMapLight(on : Boolean)

object InputMapLight {
    implicit def InputMapCodecJson : CodecJson[InputMapLight] = 
        casecodec1(InputMapLight.apply, InputMapLight.unapply) ("on")
}

case class Light(name : String, typeV : String, inputOptions : Option[InputMapLight])

object Light{
    implicit def LightCodecJson : CodecJson[Light] = 
        casecodec3(Light.apply, Light.unapply)("name", "type", "inputOptions")
}

class LightActor extends Actor {
    def receive = {
        case json : String =>
            val light : Option[Light] = json.decodeOption[Light]
            
            sender ! light.get.inputOptions.get.on
    }
}
