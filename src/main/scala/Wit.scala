import akka.actor.Actor;
import akka.actor.Props;
import akka.event.Logging;

import java.net.{URL, URLConnection, URLEncoder};
import java.io.{IOException, InputStreamReader, BufferedReader};

import spray.json._
import DefaultJsonProtocol._

class WitAIActor extends Actor with akka.actor.ActorLogging{
    private val stringUrl : String = "https://api.wit.ai/message?q="
    private var lastReceivedMessage : String = new String
 
    private def send(micData : String) = {
        var url : URL = new URL(stringUrl + URLEncoder.encode(micData, "UTF-8"));
        var uc : URLConnection = url.openConnection;

        uc.setRequestProperty("Authorization", "Bearer T34XJEJOFRNEM4JJGLURREFDSU4F7BQA");
        var inputStreamReader : InputStreamReader = new InputStreamReader(uc.getInputStream());
        var in : BufferedReader = new BufferedReader(inputStreamReader);
        
        //Reset the last sent message
        var lastSentMessage : String = ""
        
        //Read in from URL
        var inputLine : String = in.readLine;
        while (inputLine != null){
            println(inputLine)
            lastSentMessage += inputLine   
            inputLine = in.readLine
        }
        in.close();

        //Get JSON back

        lastReceivedMessage = lastSentMessage
        print(">")
    }
    def receive = {
        case micData : String =>
            send(micData)
        case _ =>
            log.info("Error: WitAIActor" + this);
    }
}
