#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiAP.h>
#include <HardwareSerial.h>
#include <ESP32Servo.h>

HardwareSerial lidarSerial(2);
#define RXD2 21 //21
#define TXD2 17
// Lidar siyah gnd, kırmızı +5V, mavi Rx, yeşil Tx.

TaskHandle_t Task1;
TaskHandle_t Task2;

const int FREQ = 1000;
const int RES  = 8;

const int channel_LF = 5;
const int channel_LB = 1;
const int channel_RF = 2;
const int channel_RB = 3;

const int left_FW  = 12;
const int left_BW  = 13;
const int right_FW = 27; 
const int right_BW = 33; 

const int servo_pin = 18;

const char *ssid = "4WD Autopilot ESP32 WiFi";
const char *password = "KvwigDEjKmT1219";
WiFiServer server(80);

int sliderValue1 = 0;
int sliderValue2 = 0;
int speeds[] = {0, 0};

uint16_t distance = 0;

Servo myservo;
int S_angle = 0;


hw_timer_t *timer = NULL;
volatile int angle = 0;
volatile bool increasing = true;



void IRAM_ATTR onTimer() 
{
  if (increasing) 
  {
    angle+=5;
    if (angle >= 179) 
    {
      increasing = false;
    }
  }
  else 
  {
    angle-=5;
    if (angle <= 1) 
    {
      increasing = true;
    }
  }
  myservo.write(angle);

  S_angle = myservo.read();
}




void setup()
{
  
  Serial.begin(115200);
  lidarSerial.begin(115200, SERIAL_8N1, RXD2, TXD2);

  server.setNoDelay(true);

  ledcSetup(channel_LF, FREQ, RES);
  ledcSetup(channel_LB, FREQ, RES);
  ledcSetup(channel_RF, FREQ, RES);
  ledcSetup(channel_RB, FREQ, RES);

  ledcAttachPin(left_FW, channel_LF);
  ledcAttachPin(left_BW, channel_LB);
  ledcAttachPin(right_FW, channel_RF);
  ledcAttachPin(right_BW, channel_RB);

  Serial.println();
  Serial.println("Configuring access point...");

  if (!WiFi.softAP(ssid, password)) {
    Serial.println("Soft AP creation failed.");
    while (1);
  }

  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  server.begin();
  Serial.println("Server started");

  myservo.attach(servo_pin);

  timer = timerBegin(0, 80, true); // 80 prescaler, 80MHz/80 = 1MHz -> 1 tick = 1us
  timerAttachInterrupt(timer, &onTimer, true);
  timerAlarmWrite(timer, 50000, true); // 5ms
  timerAlarmEnable(timer);
  xTaskCreatePinnedToCore(
    Lidar_Code,   // Task function
    "ReadSensorTask", // Name of the task
    10000,            // Stack size (in words)
    NULL,             // Task input parameter
    1,                // Priority of the task
    NULL,             // Task handle
    1                 // Core where the task should run
  );

}

void Lidar_Code(void * pvParameters) 
{
  for(;;)
  {
  uint8_t buf[9] = {0};
  if (lidarSerial.available()) 
  {
    lidarSerial.readBytes(buf, 9);
    if (buf[0] == 0x59 && buf[1] == 0x59) 
    {
      distance = buf[2] + buf[3] * 256;
    }
  }
  }
  
} // End Lidar function


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

          speeds[0] = sliderValue1;
          speeds[1] = sliderValue2;

          Serial.print(sliderValue1);
          Serial.print(" ");
          Serial.print(sliderValue2);
          Serial.print(" ");
          Serial.println(millis());

          String responseBody = String(180-angle) + " " + String(distance) + "\n";
          Serial.println(String(angle)+" "+String(S_angle)+" "+String(distance));
          client.print(responseBody);

          Drive(speeds);
        }
      }
    }
  }
}


void Drive (int speeds[])
{
  // We send PWM signals to the motors to make the vehicle move forward
  if((speeds[0] >= 0) && (speeds[1] >= 0))
  { 
    ledcWrite(channel_LF, abs(speeds[0]));            // we control the left and right engines to move forward
    ledcWrite(channel_RF, abs(speeds[1]));
    ledcWrite(channel_LB, 0);            
    ledcWrite(channel_RB, 0);               
  }

  // We send PWM signals to the motors to make the vehicle move Back
  if((speeds[0] < 0) && (speeds[1] < 0)) 
  {
    ledcWrite(channel_LF, 0);                  // we control the left and right engines to move backward
    ledcWrite(channel_RF, 0);
    ledcWrite(channel_LB, abs(speeds[0]));            
    ledcWrite(channel_RB, abs(speeds[1]));               
  }

  // We send PWM signals to the motors to make the vehicle move Right
  if((speeds[0] >= 0) && (speeds[1] < 0)) 
  {
    ledcWrite(channel_LF, abs(speeds[0]));           // We control the left engine to move forward and the right engine to move backward
    ledcWrite(channel_RF, 0);
    ledcWrite(channel_LB, 0);
    ledcWrite(channel_RB, abs(speeds[1]));                         
  }

  // We send PWM signals to the motors to make the vehicle move Left
  if((speeds[0] < 0) && (speeds[1] >= 0)) 
  {
    ledcWrite(channel_LF, 0);               // We control the left engine to move backward and the right engine to move forward
    ledcWrite(channel_RF, abs(speeds[1]));         
    ledcWrite(channel_LB, abs(speeds[0]));
    ledcWrite(channel_RB, 0);               
  }

  // We send PWM signals to the motors to make the vehicle stop
  if((speeds[0] == 0) && (speeds[1] == 0)) 
  {
    ledcWrite(channel_LF, 0);               // We control the left and right engines to stop
    ledcWrite(channel_LB, 0);               
    ledcWrite(channel_RF, 0);
    ledcWrite(channel_RB, 0);
  }
} // End Drive function

void loop() 
{
  WiFi_Code();
}