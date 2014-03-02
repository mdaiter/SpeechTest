import java.io.InputStream
import com.gtranslate.{Audio, Language}
import akka.actor.{Actor, Props, ActorSystem, ActorRef};
import akka.event.Logging;

class SpeechSynthActor extends Actor with akka.actor.ActorLogging{
    import context.system;
    private val audio : Audio = Audio.getInstance
    def receive = {
        case text : String =>
            speak(text)
    }
    private def speak(text : String) = {
        val sound : InputStream = audio.getAudio(text, Language.ENGLISH)
        audio.play(sound)
    }
}
