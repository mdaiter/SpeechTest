SpeechTest
==========

Code for our project

Hey all! If you're here, it means you're probably working on the project for the Sprint sponsor week! Here's an explanation of how this piece of software plays into the overall project

Let's delve in!

Intro - Actors
--------------------

Actors are the basis of concurrency within the project. They allow one to spawn functions or classes into a self-contained process that acts upon receiving messages. For example, say I have two actors Ping and Pong. Ping can send a message to Pong, and Pong can react to the message in a certain way. Pong can even send a message back to Ping!

The syntax for sending messages to actors is like this:

[actor to send message to] ! [message]

In other words, the exclamation point allows one to send a message from a current process to another process.

Why organize a system like this? Well, it allows for a non-threadblocking paradigm to be implemented on a distributed, concurrent, fault-tolerant system.  In other words (mainly English), actors allow your classes to run as processes that don't interrupt other processes. Your microphone class shouldn't interrupt your speech interpretation class, which shouldn't interrupt your UDP receiving class, which shouldn't interrupt your Kinect...well, you get the point! Actors allow us to program classes to instantiate and interact with---but not block the execution of---other instantiations of other objects.

Actors allow us to design a modular system to run across a network. Why would we want this stuff across a network? Well, let's say our microphone is attached to a Beaglebone Black (microcomputer) in my bedroom, and my computer is plugged into the wall in the basement where no one hears it. How should they communicate? You may say that one could use an extension cable for microphone to plug it into a computer, but then you'd need to deal with a loss of signal (and anyone who's taken 6.02 knows that the Viterbi algorithm can only do so much). The answer: communicate across the network. Spawn an actor to bind to a port on one computer, in order to receive messages from another actor on another computer. This allows your program to be disconnected from other computers across a network, while acknowledging their existence and communicating WHEN NECESSARY. This allows our system to scale across rooms, networks, houses, and even countries.

What should an actor look like?
-------------------------------------------

An actor is a class in Scala (the language I'm using for this project. Imagine Java, but cooler). It extends the class Actor in [Akka] (http://www.akka.io). You should look at their tutorial documents in order to learn the proper creation of actors in the Akka system, as they do a much better job of explaining their own system than I could. However, I will say this: you 

1. Extend an Actor to your class

2. Add in private variables and definitions in order to maintain terseness of your code

3. Make sure most---if not all---of the variables in your class are vals (think final variables in Java)

4. Define a receive function that can receive and react to messages, based on type and value

5. Launch the actor using the instantiated ActorSystem

What's an ActorSystem?
----------------------------------

An actor system allows one to keep track of all the actors within a program. Actor systems can be distributed across networks or run on the same machine. It just lets a developer make sure that the actors instantiated in the actor system don't interact with other actors instantiated in other actor systems. For example, I could run two programs that use [Akka] (http://akka.io) at the same time and be sure that running both won't block each other.

What APIs are you using?
----------------------------

1. [Wit.AI](http://wit.ai) for the speech interpretation. Works over a REST API. The JSON it sends back contains a value for an intent, and a value (usually a JSON Object) for the entity. The intent displays the purpose of the message, and the entity object shows what objects are contained in the message. Think of an intent as a verb, and the entity message as the direct objects.

2. [CMUSphinx4](http://cmusphinx.sourceforge.net/wiki/) for the speech-recognition engine. Instantiates a TargetDataLine, and waits for sound. When a sound hits a threshold, the engine starts recording until it detects a pause. The engine then interprets the sound.

3. [Flow](https://github.com/jodersky/flow) for the Serial communication with the Arduino. Using [Akka](http://akka.io) for the asynchronous receiving method.

4. [Titan](http://thinkaurelius.github.io/titan/) to store attributes and information about people in a logical manner. Discussed in the Titan section of this document.

What are the classes?
------------------------

The classes, as they currently stand, are like this:

1. MicrophoneActor - A class that lets the user connect to a microphone on a system using TargetDataLine as it's primary interface

2. WitActor - An actor that can receive a string, send it to [Wit.AI] (http://wit.ai) and send the JSON result to WitReceiverActor, or whatever actor it wants.

3. WitReceiverActor - An actor that interprets a piece of JSON it receives, based on intent and entitiy.

4. ArduinoControllerActor - Receives requests to turn pins on and off from the WitReceiverActor. It keeps track of which ArduinoActor has which pin attached, and where. Can restart an ArduinoActor if it dies.

5. ArduinoActor - Receives attributes and pins. Can turn on and off pins through Serial.

6. TitanGraphActor - Receives messages for modifying people's attributes. The actor interacts with the graph database in order to store info received from the Arduino and the Kinect

7. UDPActor - Binds to a UDP port in order to receive messages and send them off to an interpreter.

8. UDPReceiverActor - Interprets JSON messages sent to it through the UDP Actor.

More to come later...Dan wants me to crank this out now.
