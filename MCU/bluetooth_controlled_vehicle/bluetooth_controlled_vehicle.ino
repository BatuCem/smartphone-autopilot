//Global Definitions
#include <string.h>
#include <Arduino.h>
//0-3 define MCU
#define UNO 0   //select arduino board from tools
#define ESP32 1 //select ESP32 Dev Module from tools

//4-14 define bodies ->bodies include all mechanical and electronic parts
#define PROT_ARD0 4 //define arduino board setup
#define PROT_ESP0 5 //define esp32 board setup

//Global Setup
#define MODEL PROT_ESP0 //load esp32 setup
int8_t MAX_PWM=150;     //set max. PWM value
//Define Model Properties
//Arduino Prototype 0
#if (MODEL==PROT_ARD0)
const String model_name="Arduino Prototype 0";
#define MCU UNO         //select arduino UNO mcu
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
const String model_name = "ESP32 Prototype 0";
#define MCU ESP32           //select ESP32 mcu
#define analogWrite ledcWrite
//Setup Bluetooth
BluetoothSerial BTconn;
//PWM properties
const int FREQ = 5000;    //set PWM frequency
const int RES = 8;        //set PWM resolution
//use channel 0,1,2,3
const int channel_LF = 0; //CH. left-forward
const int channel_LB = 1; //CH. left-backward
const int channel_RF = 2; //CH. right-forward
const int channel_RB = 3; //CH. right-backward
//use pins 12,13,27,33 for PWM outputs
const int left_FW = 13;
const int left_BW = 12;
const int right_FW = 27;
const int right_BW = 33;

#endif
void setup(){
    //init common serial monitor
Serial.begin(9600);
    //setup bluetooth and pwm if esp32 is selected
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
    //setup pins of arduino
  #elif (MODEL==PROT_ARD0)
    pinMode(right_FW, OUTPUT);
    pinMode(right_BW, OUTPUT);
    pinMode(left_FW, OUTPUT);
    pinMode(left_BW, OUTPUT);
    pinMode(right_EN, OUTPUT);
    pinMode(left_EN, OUTPUT);
  #endif
}
//demo main for speed setting
void loop() {
 
  int speeds[]={0,0};         //define speed array
  String RX_data=RX_String(); //take string input
  if(RX_data!="NULL")         //if non-null returns
  {
    parse_Command(speeds,RX_data);  //parse string to array
    set_Speed(speeds);              //set speed from array
  }
 
  
  }

String RX_String(){ //function to take in string from bluetooth
  //read string when available
  #if (MODEL==PROT_ARD0) //for arduino model
  if (Serial.available()>0) {
    return Serial.readStringUntil('\n');
  }
  else{
    return "NULL";
  }
  #elif (MODEL==PROT_ESP0)  //for ESP32 model
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
  
  //command string of form .....'^M' 
  command.remove(command.indexOf(13)); //remove '^M'
  memset(arr,0,sizeof(arr));           //reset array
  if(command=="F"){
    //Drive forwards
    arr[0]=MAX_PWM;
    arr[1]=MAX_PWM; 
  }
  else if(command=="R")
  { 
    //Drive right
    arr[0]=MAX_PWM;
    arr[1]=-MAX_PWM; 
  }
  else if(command=="L")
  {
    //Drive left
    arr[0]=-MAX_PWM;
    arr[1]=MAX_PWM; 
  }
  else if(command=="B")
  {
    //Drive backwards
    arr[0]=-MAX_PWM;
    arr[1]=-MAX_PWM; 
  }
  else if(command=="S")
  {
    //Stop
    arr[0]=0;
    arr[1]=0; 
  }
  else
  {
    //Default case will have a reset array
    Serial.println("Unknown Command");

  }
}
void set_Speed(int* speedarr) //function to set left-right channel speeds
                    //speedarr[0]->left, speedarr[1]->right
{
  #if (MODEL==PROT_ARD0) //arduino board selected
  if(speedarr[0]>=0) //left forward
  {
    digitalWrite(left_FW, 1); //set only forward channel high
    digitalWrite(left_BW, 0);
    if(speedarr[1]>=0)//right forward
    {
      digitalWrite(right_FW, 1);//set only forward channel high
      digitalWrite(right_BW, 0);
    }
    else
    { //right backward
      digitalWrite(right_FW, 0);//set only backward channel high
      digitalWrite(right_BW, 1);
    }
  }
  else
  {//left backward
    digitalWrite(left_FW, 0);//set only backward channel high
    digitalWrite(left_BW, 1);
    if(speedarr[1]>=0)
    {//right forward
      digitalWrite(right_FW, 1);//set only forward channel high
      digitalWrite(right_BW, 0);
    }
    else
    {//right backward
      digitalWrite(right_FW, 0);//set only backward channel high
      digitalWrite(right_BW, 1);
    }
  }
  analogWrite(left_EN, abs(speedarr[0]));//set left enable PWM
  analogWrite(right_EN, abs(speedarr[1]));//set right enable PWM
  #elif (MODEL==PROT_ESP0) //esp32 board selected
     //TODO: Get rid of ledcWrite method
    if (speedarr[0]>=0 && speedarr[1] >=0)
    {//set left&right forward channels to input speed
      ledcWrite(channel_LF,abs(speedarr[0]));
      ledcWrite(channel_LB,0);
      ledcWrite(channel_RF,abs(speedarr[1]));
      ledcWrite(channel_RB,0);
    }
    else if (speedarr[0]>=0 && speedarr[1] <0)
    {//set left forward &right backward channels to input speed
      ledcWrite(channel_LF,abs(speedarr[0]));
      ledcWrite(channel_LB,0);
      ledcWrite(channel_RF,0);
      ledcWrite(channel_RB,abs(speedarr[1]));
    }
    else if (speedarr[0]<0 && speedarr[1] >=0)
    {//set right forward &left backward channels to input speed
      ledcWrite(channel_LF,0);
      ledcWrite(channel_LB,abs(speedarr[0]));
      ledcWrite(channel_RF,abs(speedarr[1]));
      ledcWrite(channel_RB,0);
    }
    else if (speedarr[0]<0 && speedarr[1] <0)
    {//set left&right backward channels to input speed
      ledcWrite(channel_LF,0);
      ledcWrite(channel_LB,abs(speedarr[0]));
      ledcWrite(channel_RF,0);
      ledcWrite(channel_RB,abs(speedarr[1]));
    }
  #endif
  
}
