#include <aJSON.h>

#include <SPI.h>         // needed for Arduino versions later than 0018
#include <Ethernet.h>
#include <EthernetUdp.h>         // UDP library from: bjoern@cs.stanford.edu 12/30/2008


// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = {  
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
IPAddress ip(10, 2, 32, 10);

unsigned int localPort = 8000;      // local port to listen on

// An EthernetUDP instance to let us send and receive packets over UDP
EthernetUDP Udp;

void setup() {
  // start the Ethernet and UDP:
  Ethernet.begin(mac,ip);
  Udp.begin(localPort);

  Serial.begin(9600);
}

void loop() {
  IPAddress ipSend(10,2,32,179);
  
  aJsonObject *root = aJson.createObject();
  aJson.addStringToObject(root, "name", "chair");
  aJson.addStringToObject(root, "typeV", "furniture");
  
  aJsonObject *inputVals = aJson.createObject();
  aJson.addNumberToObject(inputVals, "pressure", 3);
  aJson.addNumberToObject(inputVals, "temp", 64);
  aJson.addNumberToObject(inputVals, "gyro", 224);
  
  aJson.addItemToObject(root, "inputOptions", inputVals);
  // if there's data available, read a packet
  Udp.beginPacket(ipSend, 8002);
  Udp.write(aJson.print(root));
  Serial.println(strcat("String is: ", aJson.print(root)));
    //Start a json object
  Udp.endPacket();
  
  delay(10);
}

