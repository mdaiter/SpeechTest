/*
  Sample sketch communicating over Serial using JSON

  This sketch communicates over serial link with a computer, sending
  JSON-encoded state of its analog pints every second and accepting
  per-line JSON messages with desired values of PWM pins on input.

  Circuit:
  * (Optional) Analog sensors attached to analog pin.
  * (Optional) LEDs attached to PWM pins 9 and 8.

  created 1 November 2012
  by Petr Baudis

  https://github.com/interactive-matter/ajson
  This code is in the public domain.
 */

#include <aJSON.h>

unsigned long last_print = 0;
aJsonStream serial_stream(&Serial);

void setup()
{
  Serial.begin(9600);
}

/* Generate message like: { "analog": [0, 200, 400, 600, 800, 1000] } */
aJsonObject *createMessage()
{
  aJsonObject *msg = aJson.createObject();

  int analogValues[2];
  for (int i = 0; i < 2; i++) {
    analogValues[i] = bitRead(PORTB, i);
  }
  aJsonObject *analog = aJson.createIntArray(analogValues, 2);
  aJson.addItemToObject(msg, "analog", analog);

  return msg;
}
aJsonObject *createPrefsMessage(aJsonObject *recvMsg)
{
  aJsonObject *msg = aJson.createObject();

  for (int i = 0; i < 2; i++) {
    char pinNumChar[2];
    String pinNumStr = String(i+8);
    pinNumStr.toCharArray(pinNumChar, 2);
    aJson.addNumberToObject(msg, pinNumChar, bitRead(PORTB, i));
  }
  aJson.addItemToObject(msg, "names", aJson.getObjectItem(recvMsg, "names"));
  return msg;
}
/* Process message like: { "pwm": { "8": 0, "9": 128 } } */
void processMessage(aJsonObject *msg)
{
  aJsonObject *broadcast = aJson.getObjectItem(msg, "get");
  if (broadcast){
    aJson.print(createPrefsMessage(msg), &serial_stream);
  }
  else{
    
    aJsonObject *pwm = aJson.getObjectItem(msg, "pwm");
    if (!pwm) {
      return;
    }
  
    const static int pins[] = { 8, 9 };
    const static int pins_n = 2;
    for (int i = 0; i < pins_n; i++) {
      char pinstr[3];
      snprintf(pinstr, sizeof(pinstr), "%d", pins[i]);
  
      aJsonObject *pwmval = aJson.getObjectItem(pwm, pinstr);
      if (!pwmval) continue; /* Value not provided, ok. */
      if (pwmval->type != aJson_Int) {
//        Serial.print("invalid data type ");
//        Serial.print(pwmval->type, DEC);
//        Serial.print(" for pin ");
//        Serial.println(pins[i], DEC);
        continue;
      }
  
//      Serial.print("setting pin ");
//      Serial.print(pins[i], DEC);
//      Serial.print(" to value ");
//      Serial.println(pwmval->valueint, DEC);
      digitalWrite(pins[i], pwmval->valueint);
    }
  }
}

void loop()
{
  if (serial_stream.available()) {
    /* First, skip any accidental whitespace like newlines. */
    serial_stream.skip();
  }

  if (serial_stream.available()) {
    /* Something real on input, let's take a look. */
    aJsonObject *msg = aJson.parse(&serial_stream);
    processMessage(msg);
    aJson.deleteItem(msg);
  }
}
