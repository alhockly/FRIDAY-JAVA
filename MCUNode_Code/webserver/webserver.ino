    /******
code from http://randomnerdtutorials.com used
*********/
//deigned for 3.3v relays and NodeMcu

#include "ChainableLED.h"

#define NUM_LEDS  1

ChainableLED leds(14, 12, NUM_LEDS);


// Load Wi-Fi library
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <string.h>

WiFiUDP udp;

// Replace with your network credentials
const char* ssid = "Lil WiFi";
const char* password = "Despacito2";

const int dstPort = 5000;
// Set web server port number to 80
WiFiServer server(80);

// Variable to store the HTTP request
String header;

// Auxiliar variables to store the current output state
String output5State = "off";
String GPIO4State = "off";

// Assign output variables to GPIO pins
const int output5 = 5;
const int GPIO4 = 4;    //labeled as D2 on the board
const int GPIO8 = 15;
int incomingByte = 0;

int red=200;
int green=0;
int blue=200;
int lastr,lastg,lastb;



void setup() {
  lastr=red;
  lastg=green;
  lastb=blue;
  leds.init();
  leds.setColorRGB(0,red,green,blue);
  Serial.begin(9600);
  Serial.println("Hi I'm "+WiFi.macAddress());
  // Initialize the output variables as outputs
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(GPIO4, OUTPUT);

  pinMode(15, INPUT_PULLUP);
  // Set outputs states
  digitalWrite(LED_BUILTIN, LOW);
  digitalWrite(GPIO4, HIGH);

  // Connect to Wi-Fi network with SSID and password
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  int connecterr=0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    connecterr++;
    if(connecterr>100){
   
      WiFi.begin("NodeNet","");
      while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print("NodeNet.");
      
      }
      
      }


  }
  // Print local IP address and start web server
  Serial.println("");
  Serial.print("WiFi connected");
  if(connecterr>20){
  Serial.println("- NodeNet");}
  else{
    Serial.print("- ");
    Serial.println(ssid);
    }
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();
  
  IPAddress broadcastIp(255, 255, 255, 255);
  udp.beginPacketMulticast(broadcastIp, dstPort, WiFi.localIP());
  udp.write("hi");
  udp.endPacket();
}

void setRGB(int r, int g, int b){
    red=r;
    blue=b;
    green=g;
    
  }

void blink(){
  leds.setColorRGB(0,0,255,0);
  delay(300);

  }

void lights(int val){
  if(val==0){
    GPIO4State = "off";
    lastr=red;
    lastg=green;
    lastb=blue;
    setRGB(0,0,0);
    digitalWrite(GPIO4,LOW);
    }
  else if(val==1){
    GPIO4State = "on";
    if(lastr==0 && lastg==0 && lastb==0){
    lastr=200;lastb=200;  
    }
    
    setRGB(lastr,lastg,lastb);
    digitalWrite(GPIO4,HIGH);
    }
  
  
  }




void loop(){
  if(red>0 || green>0 || blue>0){
    GPIO4State="on";
    }
    else{GPIO4State="off";}
    
    leds.setColorRGB(0,red,green,blue);
    
  
   if (Serial.available() > 0) {
                // read the incoming byte:
                incomingByte = Serial.read();

                // say what you got:
                Serial.print("I received: ");
                Serial.println(incomingByte, DEC);
        }
  if(incomingByte==10){
    }
  else if(incomingByte==49){
    Serial.println("light on");
    lights(1);
    }
  else if(incomingByte==48){
    Serial.println("light off");
    lights(0);
    }
  else if(incomingByte==50){
      setRGB(255,0,0);
    }
  
  else if(incomingByte==51){
     setRGB(0,255,0);
    }
  else if(incomingByte==52){
      setRGB(125,100,0);
    }
  
  WiFiClient client = server.available();   // Listen for incoming clients

  //Serial.println(digitalRead(15));
  if (client) {                             // If a new client connects,
    Serial.println("New Client.");          // print a message out in the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        char c = client.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        header += c;
        if (c == '\n') {                    // if the byte is a newline character
          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println("Connection: close");
            client.println();

            char copy[20];
            header.toCharArray(copy,20);
            
            // turns the GPIOs on and off
            if(header.indexOf("GET /blue")>=0){
              blue+=50;
              if(blue>255){blue=0;}
            }
            else if(header.indexOf("GET /green")>=0){
              green+=50;
              if(green>255){green=0;}
            }
            else if(header.indexOf("GET /red")>=0){
              red+=50;
              if(red>255){red=0;}
             
            }

            if(red>0 || green>0 || blue>0){
              GPIO4State="on";
            }
            
            if (header.indexOf("GET /5/on") >= 0) {
              Serial.println("GPIO 5 on");
              output5State = "on";
              digitalWrite(LED_BUILTIN, HIGH);
            } else if (header.indexOf("GET /5/off") >= 0) {
              Serial.println("GPIO 5 off");
              output5State = "off";
              digitalWrite(LED_BUILTIN, LOW);
            } else if (header.indexOf("GET /on") >= 0) {
              Serial.println("GPIO 4 on");
              GPIO4State = "on";
              lights(1);
            } else if (header.indexOf("GET /off") >= 0) {
              Serial.println("GPIO 4 off");
              GPIO4State = "off";
              lights(0);
            }else if (header.indexOf("GET /blink") >= 0) {
              Serial.println("blinking");
              blink();
            }
            
            else if (strstr(copy, "netconf?ssid=") != NULL) {
                  // contains
                  Serial.println("NEW Deeeets");
                }{
              }
            
            
            // Display the HTML web page
            client.println("<!DOCTYPE html><html>");
            client.println("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            client.println("<link rel=\"icon\" href=\"data:,\">");
            // CSS to style the on/off buttons 
            // Feel free to change the background-color and font-size attributes to fit your preferences
            client.println("<style>html { font-family: Helvetica; display: inline-block; margin: 0px auto; text-align: center;}");
            client.println(".button { background-color: #195B6A; border: none; color: white; padding: 16px 40px;");
            client.println("text-decoration: none; font-size: 30px; margin: 2px; cursor: pointer;}");
            client.println(".button2 {background-color: #77878A;}</style></head>");
            
            // Web Page Heading
            client.println("<body><h1>ESP8266 Web Server</h1>");
            client.print("<a href='/red'<p>red:");
            client.print(red);
            client.print("</p></a><a href='/green'><p>green:");
            client.print(green);
            client.print("</p></a><a href='/blue'><p>blue:");
            client.print(blue);
            client.print("</p></a>");
            // Display current state, and ON/OFF buttons for GPIO 5  
            client.println("<p>Built in LED - State " + output5State + "</p>");
            // If the output5State is off, it displays the ON button       
            if (output5State=="off") {
              client.println("<p><a href=\"/5/on\"><button class=\"button\">ON</button></a></p>");
            } else {
              client.println("<p><a href=\"/5/off\"><button class=\"button button2\">OFF</button></a></p>");
            } 
               
            // Display current state, and ON/OFF buttons for GPIO 4  
            client.println("<p>Pin D2(Lights) - State " + GPIO4State + "</p>");
            // If the GPIO4State is off, it displays the ON button       
            if (GPIO4State=="off") {
              client.println("<p><a href=\"/on\"><button class=\"button\">ON</button></a></p>");
            } else {
              client.println("<p><a href=\"/off\"><button class=\"button button2\">OFF</button></a></p>");
            }
            client.println(WiFi.macAddress());
            
            
            client.println("<br><h3>Set new network</h3>");
            
            client.println("<table><tr><th>ssid</th><th>pass</th></tr></table>");
            client.println("<form action=\"netconf\"><input type=\"text\" name=\"ssid\"><input type=\"text\" name=\"pass\"><br><input type=\"submit\" value=\"Submit\"></form>");
            client.println("</body></html>");
            
            // The HTTP response ends with another blank line
            client.println();
            // Break out of the while loop
            break;
          } else { // if you got a newline, then clear currentLine
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }
      }
    }
    // Clear the header variable
    header = "";
    // Close the connection
    client.stop();
    Serial.println("Client disconnected.");
    Serial.println("");
  }
}
