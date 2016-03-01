

const int analogInPin = A0;  // Analog input pin that the potentiometer is attached to

int sensorValue = 0; // value read
int i=1;

boolean sendADC=false;

#include <SoftwareSerial.h>
SoftwareSerial mySerial(10, 11); // RX, TX

void setup() {
  // initialize serial communications at 9600 bps:
  Serial.begin(9600); 
  

  // set the data rate for the SoftwareSerial port
  mySerial.begin(9600);
}

void loop() {
  // get the new byte:
  int inChar = mySerial.read(); 
     
  if(sendADC){
  // read the analog in value:
  sensorValue = analogRead(analogInPin);
  // print the results to the serial monitor:             
  mySerial.println(sensorValue);
  i++;  
  }
  if(i>=12000){
  i=1;
  sendADC=false;}
  if(inChar==65&&!sendADC){
  mySerial.print("B");                       
  sendADC=true;
  inChar=0;}
  else if(inChar==67&&sendADC){
  mySerial.print("D");
  sendADC=false;  
  }
  // wait 1 milliseconds before the next loop
  // for the analog-to-digital converter to settle
  // after the last reading:
  delay(1);                     
}
