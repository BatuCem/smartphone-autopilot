#include <WiFi.h>           // We include the WiFi library.
#include <WiFiClient.h>     // We include the WiFiClient library.
#include <WiFiAP.h>         // We include the WiFiAP library.

#define analogWrite ledcWrite  // We define 'ledcWrite' to be used instead of 'analogWrite'.


int8_t MAX_PWM = 120;    // Defines and Sets PWM 
const int FREQ = 1000;     // Defines and Sets Frequency 
const int RES  = 8;      // Defines and Sets Resoluation 

const int channel_LF = 0; //CH. left-forward
const int channel_LB = 1; //CH. left-backward
const int channel_RF = 2; //CH. right-forward
const int channel_RB = 3; //CH. right-backward

const int left_FW  = 13;  // Description of Left-Forward Port
const int left_BW  = 12;  // Description of Left-Backward Port
const int right_FW = 27;  // Description of Right-Forward Port
const int right_BW = 33;  // Description of Right-Backward Port

const char *ssid     = "ESP-32";    // Name of Wi-Fi Network
const char *password = "password";  // Password of Wi-Fi Network
  // You can remove the password parameter if you want the AP to be open.
  // a valid password must have more than 7 characters

WiFiServer server(80);   // Sets Port 80 for the web server.



const int trigger_pin = 35;   // Description of trigger Port for Ultrasonic Sensor
const int echo_pin    = 34;   // Description of echo Port for Ultrasonic Sensor

int time_1;                   // The time value when we measure the appearance of the echo signal
int distance;                 // The distance information we will calculate



void setup() {
 
  ledcSetup(channel_LF,FREQ,RES);   // Sets up frequency and resolution signals to the Left Forward channel
  ledcSetup(channel_LB,FREQ,RES);   // Sets up frequency and resolution signals to the Left Backward channel
  ledcSetup(channel_RF,FREQ,RES);   // Sets up frequency and resolution signals to the Right Forward channel
  ledcSetup(channel_RB,FREQ,RES);   // Sets up frequency and resolution signals to the Right Backward channel

  ledcAttachPin(left_FW,channel_LF);   // Setting the Left Forward Channel to the Left Forward pin
  ledcAttachPin(left_BW,channel_LB);   // Setting the Left Backward Channel to the Left Backward pin
  ledcAttachPin(right_FW,channel_RF);  // Setting the Right Forward Channel to the Right Forward pin
  ledcAttachPin(right_BW,channel_RB);  // Setting the Right Backward Channel to the Right Backward pin
  

  Serial.begin(115200);   // Initializes the serial communication line and sets it to 115200 baud rate
  Serial.println();       // Sends an empty line to the serial communication line
  Serial.println("Configuring access point..."); //Sends the message to the serial communication line

  if (!WiFi.softAP(ssid, password)) { 
    log_e("Soft AP creation failed.");   // If the creation of the Soft AP (Access Point) fails, log an error
    while(1);                            // Enter an infinite loop to halt the program
  }
  IPAddress myIP = WiFi.softAPIP();      // Obtain the IP address of the Soft AP
  Serial.print("AP IP address: ");
  Serial.println(myIP);                  // Print the IP address of the Soft AP to the serial monitor.
  server.begin();                        // Start the server to handle incoming client requests

  Serial.println("Server started");      // Print a message indicating that the server has started
  
  pinMode(trigger_pin, OUTPUT);          // Set the trigger pin as an output
  pinMode(echo_pin   , INPUT);           // Set the echo pin as an input
  
}



void loop() {
  
  WiFiClient client = server.available();   // listen for incoming clients

  if (client) {                             // if you get a client,
    Serial.println("New Client.");          // print a message out the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        char c = client.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");             
            client.println("Content-type:text/html");
            client.println();

           // Sends us commands by adding a message and a button to the web browser
            client.print("Click <a href=\"/F\">here</a> to go Forward.<br>");   
            client.print("Click <a href=\"/B\">here</a> to go Back.<br>");      
            client.print("Click <a href=\"/R\">here</a> to go Right.<br>");
            client.print("Click <a href=\"/L\">here</a> to go Left.<br>");
            client.print("Click <a href=\"/S\">here</a> to Stop.<br>");

            // The HTTP response ends with another blank line:
            client.println();
            // break out of the while loop:
            break;
          } else {    // if you got a newline, then clear currentLine:
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }

        // We send PWM signals to the motors to make the vehicle move forward
        if (currentLine.endsWith("GET /F")) { 
          ledcWrite(channel_LF, MAX_PWM);            // we control the left and right engines to move forward
          ledcWrite(channel_RF, MAX_PWM);
          ledcWrite(channel_LB, 0);            
          ledcWrite(channel_RB, 0);               
        }
      

        // We send PWM signals to the motors to make the vehicle move Back
         if (currentLine.endsWith("GET /B")) {
          ledcWrite(channel_LF, 0);                  // we control the left and right engines to move backward
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_LB, MAX_PWM);            
          ledcWrite(channel_RB, MAX_PWM);               
        }

        // We send PWM signals to the motors to make the vehicle move Right
        if (currentLine.endsWith("GET /R")) {
          ledcWrite(channel_LF, MAX_PWM);           // We control the left engine to move forward and the right engine to move backward
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_LB, 0);
          ledcWrite(channel_RB, MAX_PWM);                         
        }

        // We send PWM signals to the motors to make the vehicle move Left
        if (currentLine.endsWith("GET /L")) {
          ledcWrite(channel_LF, 0);               // We control the left engine to move backward and the right engine to move forward
          ledcWrite(channel_RF, MAX_PWM);         
          ledcWrite(channel_LB, MAX_PWM);
          ledcWrite(channel_RB, 0);               
        }

        // We send PWM signals to the motors to make the vehicle stop
        if (currentLine.endsWith("GET /S")) {
          ledcWrite(channel_LF, 0);               // We control the left and right engines to stop
          ledcWrite(channel_LB, 0);               
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_RB, 0);
        }

      }
    }
    // close the connection:
    client.stop();
    Serial.println("Client Disconnected.");  // prints the message to disconnect the connection
  }

  digitalWrite(trigger_pin, HIGH);          // Set the trigger pin to HIGH to generate an ultrasonic pulse
  delayMicroseconds(1000);                  // Keep it HIGH for a short duration
  digitalWrite(trigger_pin, LOW);           // Turn off the trigger signal
  time_1   = pulseIn(echo_pin, HIGH);       // Measure the time it takes for the echo signal to return
  distance = ((time_1 / 2) / 29.1);         // Calculate the distance based on the time measured
 
  Serial.print(" Object distance = ");      // Print the distance to the serial monitor
  Serial.print(distance);
  delay(1);

// If the distance measured by the sensor falls below 30 (in our system, 30 cm), it sends a stop command to the motors
  if ( distance <= 30){
    ledcWrite(channel_LF, 0);               // We control the left and right engines to stop
    ledcWrite(channel_LB, 0);               
    ledcWrite(channel_RF, 0);
    ledcWrite(channel_RB, 0);
  }
  
}
