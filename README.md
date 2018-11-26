# FRIDAY-JAVA
NLP interface for IoT

Java+Google Speech API+NodeMcu(ESP8266)+P9813 based LED Strip driver


All libraries are open source and active so its advisable to get new versions if possible

The voice recognition grammmar is built from the xml nodes
e.g
```
<XML>
<Node>
  <FName>lights</FName>
  <Mac>A0:20:A6:02:_2:_6</Mac>
  <IP>100.71.194.167</IP>
  <Verbs>switch,turn</Verbs>
  <Vals>on,off,blink</Vals>
</Node>
</XML>
```
If user says a FName and a verb followed by a Val then the value is send to the corresponding node

# Current functionality
- Find all Nodes on a local network (brute force + subnet clues)
- Communication by http with Node
- Porcupine hotword detection /Google speech API
- Webserver interface


FRIDAY is my ongoing attempt at creating an AI. The main functionality is around web interfacing and typical PA type things, but the eventual goal is to combine this with learning algorithms that will allow FRIDAY's activity to be intuitively programmed by the user.
