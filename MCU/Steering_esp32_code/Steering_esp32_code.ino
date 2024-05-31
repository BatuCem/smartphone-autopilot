#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiAP.h>
#include <HardwareSerial.h>
#include <ESP32Servo.h>

HardwareSerial lidarSerial(2);
#define RXD2 16
#define TXD2 19
// Lidar siyah gnd, kırmızı +5V, mavi Rx, yeşil Tx.

TaskHandle_t Task1;
TaskHandle_t Task2;

const int FREQ = 1000;
const int RES  = 8;

const int channel_MF = 2;
const int channel_MB = 3;

const int motor_FW  = 33;
const int motor_BW  = 27;

const char *ssid = "Steering Autopilot ESP32 WiFi";
const char *password = "KvwigDEjKmT1219";
WiFiServer server(80);

int sliderValue1 = 0;
int sliderValue2 = 0;

uint16_t distance = 0;

Servo steeringServo;
int S_angle = 0;


hw_timer_t *timer = NULL;
volatile int angle = 0;
volatile bool increasing = true;





void setup()
{
  
  Serial.begin(115200);

  server.setNoDelay(true);

  ledcSetup(channel_MF, FREQ, RES);
  ledcSetup(channel_MB, FREQ, RES);

  ledcAttachPin(motor_FW, channel_MF);
  ledcAttachPin(motor_BW, channel_MB);

  Serial.println();
  Serial.println("Configuring access point...");

  if (!WiFi.softAP(ssid, password)) {
    Serial.println("Soft AP creation failed.");
    while (1);
  }

  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  server.begin();er started");

  Serial.println("Serv
  steeringServo.attach(17);
  steeringServo.write(100);

}


void WiFi_Code() 
{
  WiFiClient client = server.available();
  if (client)
  {
    while(client.connected())
    {
      if(client.available())
      {
        String command = client.readStringUntil('\n');
        //Serial.println(command);
        if (command.length() >=8) 
        {
          String firstValue = command.substring(0, 4);
          String secondValue = command.substring(4, 8);

          sliderValue1 = firstValue.toInt();
          sliderValue2 = secondValue.toInt();

          Serial.print(sliderValue1);
          Serial.print(" ");
          Serial.print(sliderValue2);
          Serial.print(" ");
          Serial.println(millis());

          String responseBody = String(angle) + " " + String(distance) + "\n";
          Serial.println(String(angle)+" "+String(S_angle)+" "+String(distance));
          client.print(responseBody);

          SteeringDrive(sliderValue1,sliderValue2);
        }
      }
    }
  }
}


void SteeringDrive (int rotation, int speed)
{
  if(rotation>=120)
  {
    steeringServo.write(120);
  }
  else if (rotation <=80)
  {
    steeringServo.write(80);
  }
  else{
    steeringServo.write(rotation);
  }
  if(speed >= 0)
  {
    ledcWrite(channel_MF,abs(speed));
    ledcWrite(channel_MB,0);
  }
  else
  {
    ledcWrite(channel_MF, 0);
    ledcWrite(channel_MB, abs(speed));
  }
} // End Drive function

void loop() 
{
  WiFi_Code();
}