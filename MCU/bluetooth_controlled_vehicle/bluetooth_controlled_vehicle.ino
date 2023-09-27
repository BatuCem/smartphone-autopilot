//Global Definitions
#include <string.h>
#include <Arduino.h>
//0-3 define MCU
#define UNO 0   //select arduino board
#define ESP32 1 //select ESP32 Dev Module

//4-14 define bodies ->bodies include all mechanical and electronic parts
#define PROT_ARD0 4
#define PROT_ESP0 5

//Global Setup
#define MODEL PROT_ESP0
int8_t MAX_Speed=60;
//Define Model Properties
//Arduino Prototype 0
#if (MODEL==PROT_ARD0)
const String model_name="Arduino Prototype 0";
#define MCU UNO
//Arduino Pins
const int left_EN = 11;  // ENABLE A
const int right_FW = 10;   // INPUT 1
const int right_BW = 9;     // INPUT 2
const int left_FW = 8;    // INPUT 3
const int left_BW = 7;     // INPUT 4
const int right_EN = 6;   // ENABLE B

//ESP32 Prototype 0
#elif (MODEL == PROT_ESP0)
#include <BluetoothSerial.h>
const String robot_type = "ESP32 Prototype 0";
#define MCU ESP32
#define analogWrite ledcWrite
/*#define attachPinChangeInterrupt attachInterrupt
#define detachPinChangeInterrupt detachInterrupt
#define digitalPinToPinChangeInterrupt digitalPinToInterrupt
#define PIN_PWM_L1 CH_PWM_L1
#define PIN_PWM_L2 CH_PWM_L2
#define PIN_PWM_R1 CH_PWM_R1
#define PIN_PWM_R2 CH_PWM_R2*/
//Setup Bluetooth
BluetoothSerial BTconn;
//PWM properties
const int FREQ = 1000;
const int RES = 8;
const int channel_LF = 0;
const int channel_LB = 1;
const int channel_RF = 2;
const int channel_RB = 3;
const int left_FW = 13;
const int left_BW = 12;
const int right_FW = 27;
const int right_BW = 33;

#endif
void setup(){
  #if (MODEL==PROT_ESP0) 
    ledcSetup(channel_LF,FREQ,RES);
    ledcSetup(channel_LB,FREQ,RES);
    ledcSetup(channel_RF,FREQ,RES);
    ledcSetup(channel_RB,FREQ,RES);

    ledcAttachPin(left_FW,channel_LF);
    ledcAttachPin(left_BW,channel_LB);
    ledcAttachPin(right_FW,channel_RF);
    ledcAttachPin(right_BW,channel_RB);

    BTconn.begin("ESP32 Autopilot");
  #elif (MODEL==PROT_ARD0)
    pinMode(right_FW, OUTPUT);
    pinMode(right_BW, OUTPUT);
    pinMode(left_FW, OUTPUT);
    pinMode(left_BW, OUTPUT);
    pinMode(right_EN, OUTPUT);
    pinMode(left_EN, OUTPUT);
  #endif
Serial.begin(9600);
}
void loop() {
  
  int speeds[]={0,0};
  String RX_data=RX_String();
  if(RX_data!="NULL")
  {
    parse_Command(speeds,RX_data);
    set_Speed(speeds);
  }
 
  
  }

String RX_String(){
  #if (MODEL==PROT_ARD0)
  if (Serial.available()>0) {
    return Serial.readStringUntil('\n');
    
  }
  else{
    return "NULL";
  }
  #elif (MODEL==PROT_ESP0)
  if(BTconn.available())
  {
    return BTconn.readStringUntil('\n');
  }
  else
  {
    return "NULL";
  }
  #endif
}

void parse_Command(int* arr,String command){    //TODO: recognize command(input1,input2,...)
  
   
  command.remove(command.indexOf(13));
  memset(arr,0,sizeof(arr));
  if(command=="F"){
    arr[0]=MAX_Speed;
    arr[1]=MAX_Speed; 
  }
  else if(command=="R")
  { 
    arr[0]=MAX_Speed;
    arr[1]=-MAX_Speed; 
  }
  else if(command=="L")
  {
    arr[0]=-MAX_Speed;
    arr[1]=MAX_Speed; 
  }
  else if(command=="B")
  {
    arr[0]=-MAX_Speed;
    arr[1]=-MAX_Speed; 
  }
  else if(command=="S")
  {
    arr[0]=0;
    arr[1]=0; 
  }
  else
  {
    Serial.println("Unknown Command");

  }
}
void set_Speed(int* speedarr)
{
  #if (MODEL==PROT_ARD0)
  if(speedarr[0]>=0)
  {
    digitalWrite(left_FW, 1);
    digitalWrite(left_BW, 0);
    if(speedarr[1]>=0)
    {
      digitalWrite(right_FW, 1);
      digitalWrite(right_BW, 0);
    }
    else
    {
      digitalWrite(right_FW, 0);
      digitalWrite(right_BW, 1);
    }
  }
  else
  {
    digitalWrite(left_FW, 0);
    digitalWrite(left_BW, 1);
    if(speedarr[1]>=0)
    {
      digitalWrite(right_FW, 1);
      digitalWrite(right_BW, 0);
    }
    else
    {
      digitalWrite(right_FW, 0);
      digitalWrite(right_BW, 1);
    }
  }
  analogWrite(left_EN, abs(speedarr[0]));
  analogWrite(right_EN, abs(speedarr[1]));
  #elif (MODEL==PROT_ESP0)
    if (speedarr[0]>=0 && speedarr[1] >=0)
    {
      ledcWrite(channel_LF,abs(speedarr[0]));
      ledcWrite(channel_LB,0);
      ledcWrite(channel_RF,abs(speedarr[1]));
      ledcWrite(channel_RB,0);
    }
    else if (speedarr[0]>=0 && speedarr[1] <0)
    {
      ledcWrite(channel_LF,abs(speedarr[0]));
      ledcWrite(channel_LB,0);
      ledcWrite(channel_RF,0);
      ledcWrite(channel_RB,abs(speedarr[1]));
    }
    else if (speedarr[0]<0 && speedarr[1] >=0)
    {
      ledcWrite(channel_LF,0);
      ledcWrite(channel_LB,abs(speedarr[0]));
      ledcWrite(channel_RF,abs(speedarr[1]));
      ledcWrite(channel_RB,0);
    }
    else if (speedarr[0]<0 && speedarr[1] <0)
    {
      ledcWrite(channel_LF,0);
      ledcWrite(channel_LB,abs(speedarr[0]));
      ledcWrite(channel_RF,0);
      ledcWrite(channel_RB,abs(speedarr[1]));
    }
  #endif
  
}
