import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import akka.actor.{Actor, Props, ActorSystem};
import akka.event.Logging;

class SpeechActor extends Actor with akka.actor.ActorLogging{

    private var cm : ConfigurationManager = new ConfigurationManager(getClass.getResource("helloworld.config.xml"));
    private var recognizer : Recognizer = cm.lookup("recognizer").asInstanceOf[Recognizer];
    private var mic : Microphone = cm.lookup("microphone").asInstanceOf[Microphone];
    private var lastString : String = new String();
    private def start : Boolean = {
        /*
        * Instantiate variables
        */

        recognizer.allocate();
        if (!mic.startRecording()){
            recognizer.deallocate();
            return false;
        }
        return true;
    }
    private def stop : Boolean = {
        mic.stopRecording();
        recognizer.deallocate();
        return true;
    }

    private def grabVal : String= {
        mic.clear();
        var result : Result = recognizer.recognize();
        if (result != null){
            lastString = result.getBestFinalResultNoFiller;
            return result.getBestFinalResultNoFiller;
        }
        else{
            return null;
        }
    }

    def receive = {
        case "start" =>
            sender ! start;
        case "get" =>
            log.info("received get function");
            mic.clear();
            var result : Result = recognizer.recognize();
            if (result != null){
                sender ! result.getBestFinalResultNoFiller();
            }
            else{
                sender ! "failed"
            }
        case "stop" =>
            sender ! stop;
        case _ =>
            log.info("received unknown message");
    }
}

object Speech extends App{
    val system = ActorSystem("Speech")
    val helloWorldActor = system.actorOf(Props[WitAIActor])
    printf("Input a string you want analyzed.\n>")
    while(true){
        var read : String = readLine
        helloWorldActor ! read
    }
}
