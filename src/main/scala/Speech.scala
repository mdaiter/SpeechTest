import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import akka.actor.{Actor, Props, ActorSystem, ActorRef};
import akka.event.Logging;

class SpeechActor extends Actor with akka.actor.ActorLogging{
    import context.system;
    private val cm : ConfigurationManager = new ConfigurationManager(getClass.getResource("/helloworld.config.xml"));
    private var recognizer : Recognizer = cm.lookup("recognizer").asInstanceOf[Recognizer]
    private var mic : Microphone = cm.lookup("microphone").asInstanceOf[Microphone]
    private var lastString : String = new String();
    private def start : Boolean = {
        /*
        * Instantiate variables
        */
        recognizer.allocate();
        mic = cm.lookup("microphone").asInstanceOf[Microphone]
        if (!mic.startRecording()){
            println("Didn't hit here")
            recognizer.deallocate();
            return false;
        }
        println("Hit here!")
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
    def loop (sender : ActorRef) = {
        while (true) {
            log.info("Starting to log mic info")
            //mic.clear
            val result : Result = recognizer.recognize
            if (result != null){
                val micRead = result.getBestFinalResultNoFiller
                log.info("Got new mic reading: " ++ micRead)
                sender ! micRead
            }
            else{
                log.info("I think I misheard you...")
            }
        }
    }
    def receive = {
        case "start" =>
            this.start
            loop(sender)
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
    val system = ActorSystem("mySystem")
    val helloWorldActor = system.actorOf(Props[WitAIActor])
    val udpInterpreter = system.actorOf(Props[UDPReceiver])
    val camActor = system.actorOf(Props(new UDPActor(8008, udpInterpreter)), name="FaceActor")
    val arduinoActor = system.actorOf(Props ( new ArduinoActor("/dev/ttyACM0")), name="Arduino")
    printf("Input a string you want analyzed.\n>")
    while(true){
        var read : String = readLine
        helloWorldActor ! read
    }
}
