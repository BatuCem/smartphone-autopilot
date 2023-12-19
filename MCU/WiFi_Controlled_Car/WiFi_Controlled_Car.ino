#include <WiFi.h>           // We include the WiFi library.
#include <WiFiClient.h>     // We include the WiFiClient library.
#include <WiFiAP.h>         // We include the WiFiAP library.
#include <string.h>

#define analogWrite ledcWrite  // We define 'ledcWrite' to be used instead of 'analogWrite'.


int8_t MAX_PWM = 120;    // Defines and Sets PWM 
const int FREQ = 1000;     // Defines and Sets Frequency 
const int RES  = 8;      // Defines and Sets Resoluation 

const int channel_LF = 0; //CH. left-forward
const int channel_LB = 1; //CH. left-backward
const int channel_RF = 2; //CH. right-forward
const int channel_RB = 3; //CH. right-backward

const int left_FW  = 33;  // Description of Left-Forward Port
const int left_BW  = 27;  // Description of Left-Backward Port
const int right_FW = 12;  // Description of Right-Forward Port
const int right_BW = 13;  // Description of Right-Backward Port


const char *ssid     = "ESP-32";    // Name of Wi-Fi Network
const char *password = "password";  // Password of Wi-Fi Network
  // You can remove the password parameter if you want the AP to be open.
  // a valid password must have more than 7 characters

WiFiServer server(80);   // Sets Port 80 for the web server.

int sliderValue1 = 0; // İlk kaydırıcının değeri için değişken
int sliderValue2 = 0; // İkinci kaydırıcının değeri için değişken

int speeds[] = {0,0};

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
  
}



void loop() {
  
  WiFiClient client = server.available();   // listen for incoming clients

  if (client) {
    String command = client.readStringUntil('\n');
    client.flush();
    Serial.println(command); 
    
    if (command.startsWith("GET /")) {
        int firstSlashIndex = command.indexOf('/'); // İlk eğik çizginin konumunu bul
        int secondSlashIndex = command.indexOf('/', firstSlashIndex + 1); // İkinci eğik çizginin konumunu bul
        
        if (firstSlashIndex != -1 && secondSlashIndex != -1) {
            String values = command.substring(firstSlashIndex + 1, secondSlashIndex); // İki değer arasındaki kısmı al
            
            String firstValue = values.substring(0, 4); // İlk 4 haneli değeri al
            String secondValue = values.substring(4, 8); // Sonraki 4 haneli değeri al
            
            int sliderValue1 = firstValue.toInt(); // İlk değeri tamsayıya dönüştür
            int sliderValue2 = secondValue.toInt(); // İkinci değeri tamsayıya dönüştür
            
            Serial.print("value1=");
            Serial.println(sliderValue1); // İlk değeri seri porta yazdır
            Serial.print("value2=");
            Serial.println(sliderValue2); // İkinci değeri seri porta yazdır

            speeds[0] = sliderValue1;
            speeds[1] = sliderValue2;
            Serial.println(speeds[0]); 
            Serial.println(speeds[1]); 

            Drive(speeds);
        }
    }
  }
}
  

void Drive (int speeds[]){
        // We send PWM signals to the motors to make the vehicle move forward
        if ((speeds[0] >= 0) && (speeds[1] >= 0)){ 
          ledcWrite(channel_LF, abs(speeds[0]));            // we control the left and right engines to move forward
          ledcWrite(channel_RF, abs(speeds[1]));
          ledcWrite(channel_LB, 0);            
          ledcWrite(channel_RB, 0);               
        }
        

        // We send PWM signals to the motors to make the vehicle move Back
         if ((speeds[0] < 0) && (speeds[1] < 0)) {
          ledcWrite(channel_LF, 0);                  // we control the left and right engines to move backward
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_LB, abs(speeds[0]));            
          ledcWrite(channel_RB, abs(speeds[1]));               
        }

        // We send PWM signals to the motors to make the vehicle move Right
        if ((speeds[0] >= 0) && (speeds[1] < 0)) {
          ledcWrite(channel_LF, abs(speeds[0]));           // We control the left engine to move forward and the right engine to move backward
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_LB, 0);
          ledcWrite(channel_RB, abs(speeds[1]));                         
        }

        // We send PWM signals to the motors to make the vehicle move Left
        if ((speeds[0] < 0) && (speeds[1] >= 0)) {
          ledcWrite(channel_LF, 0);               // We control the left engine to move backward and the right engine to move forward
          ledcWrite(channel_RF, abs(speeds[1]));         
          ledcWrite(channel_LB, abs(speeds[0]));
          ledcWrite(channel_RB, 0);               
        }

        // We send PWM signals to the motors to make the vehicle stop
        if ((speeds[0] == 0) && (speeds[1] == 0)) {
          ledcWrite(channel_LF, 0);               // We control the left and right engines to stop
          ledcWrite(channel_LB, 0);               
          ledcWrite(channel_RF, 0);
          ledcWrite(channel_RB, 0);
        }

}
