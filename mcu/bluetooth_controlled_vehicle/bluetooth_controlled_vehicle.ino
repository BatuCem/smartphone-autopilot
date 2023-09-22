/* Bluetooth Controlled Vehicle */
const int left_EN = 11;  // ENABLE A
const int right_FW = 10;   // INPUT 1
const int right_BW = 9;     // INPUT 2
const int left_FW = 8;    // INPUT 3
const int left_BW = 7;     // INPUT 4
const int right_EN = 6;   // ENABLE B

void setup(){
pinMode(right_FW, OUTPUT);
pinMode(right_BW, OUTPUT);
pinMode(left_FW, OUTPUT);
pinMode(left_BW, OUTPUT);
pinMode(right_EN, OUTPUT);
pinMode(left_EN, OUTPUT);
Serial.begin(9600);
}

void loop() {
if (Serial.available() > 0) {
char read_BL_serial = Serial.read();
Serial.println(read_BL_serial);

if( read_BL_serial == 'F' ) { // if read_BL_serial 'F'
/* araç ileri gitsin */
digitalWrite(right_FW, 1);
digitalWrite(right_BW, 0);
digitalWrite(left_FW, 1);
digitalWrite(left_BW, 0);
analogWrite(right_EN, 255); // motor speeds 255 PWM
analogWrite(left_EN, 255);
}

if( read_BL_serial == 'R' ) { //if read_BL_serial 'R'
/* araç sağa dönsün */
digitalWrite(right_FW, 0);
digitalWrite(right_BW, 1);
digitalWrite(left_FW, 1);
digitalWrite(left_BW, 0);
analogWrite(right_EN, 200); // motor speeds 200 PWM
analogWrite(left_EN, 200);
}

if( read_BL_serial == 'L' ) { //if read_BL_serial 'L'
/* araç sola dönsün */
digitalWrite(right_FW, 1);
digitalWrite(right_BW, 0);
digitalWrite(left_FW, 0);
digitalWrite(left_BW, 1);
analogWrite(right_EN, 200); // motor speeds 200 PWM
analogWrite(left_EN, 200);
}

if( read_BL_serial == 'B' ) { // if read_BL_serial 'B'
/* araç geri gitsin */
digitalWrite(right_FW, 0);
digitalWrite(right_BW, 1);
digitalWrite(left_FW, 0);
digitalWrite(left_BW, 1);
analogWrite(right_EN, 255); // motor speeds 255 PWM
analogWrite(left_EN, 255);
}

if( read_BL_serial == 'S' ) { // if read_BL_serial 'S'
/* araç dursun */
digitalWrite(right_FW, 0);
digitalWrite(right_BW, 0);
digitalWrite(left_FW, 0);
digitalWrite(left_BW, 0);   //stop
}
}
}
