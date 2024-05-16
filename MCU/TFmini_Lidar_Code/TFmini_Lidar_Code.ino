#include <HardwareSerial.h> // Reference the ESP32 built-in serial port library
HardwareSerial lidarSerial(2); // Using serial port 2
#define RXD2 16
#define TXD2 17

const int ledPin = 25; // LED pin
uint16_t distance; // Distance measured by the LiDAR

void controlLED() 
{
  // This function acts like an interrupt handler
  if (distance < 20) 
  {
    digitalWrite(ledPin, HIGH); // Turn on the LED if distance is less than 20 cm
  } 
  else 
  {
    digitalWrite(ledPin, LOW); // Otherwise, turn off the LED
  }
}

void setup() 
{
  Serial.begin(115200);
  lidarSerial.begin(115200, SERIAL_8N1, RXD2, TXD2); // Initializing serial port for LiDAR

  pinMode(ledPin, OUTPUT); // Set LED pin as output
}

void loop() 
{
  uint8_t buf[9] = {0}; // Buffer to store data from LiDAR

  if (lidarSerial.available() > 0) 
  {
    lidarSerial.readBytes(buf, 9); // Read 9 bytes of data from LiDAR

    if (buf[0] == 0x59 && buf[1] == 0x59) 
    {
      distance = buf[2] + buf[3] * 256; // Calculate distance from the data
      Serial.println(distance); // Print the distance to the serial monitor
      controlLED(); // Act upon the distance value as if it were an interrupt
    }
  }
}
