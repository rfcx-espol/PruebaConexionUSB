void setup() {
  Serial.begin(9600);  
  //dht.begin();
  
}
float h =0;
float t = 8;
 
void loop() {
 
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  h = h+0.06;
  t = t+0.02;
  delay(1000);
 
  // check if returns are valid, if they are NaN (not a number) then something went wrong!

    Serial.print("H:"); 
    Serial.print(h);
    Serial.print(",");
    Serial.print("T:"); 
    Serial.print(t);
    Serial.println(";");
  
}
