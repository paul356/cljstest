/*
  Blink
  Turns on an LED on for one second, then off for one second, repeatedly.

  Most Arduinos have an on-board LED you can control. On the Uno and
  Leonardo, it is attached to digital pin 13. If you're unsure what
  pin the on-board LED is connected to on your Arduino model, check
  the documentation at http://www.arduino.cc

  This example code is in the public domain.

  modified 8 May 2014
  by Scott Fitzgerald
 */
#define PARAM_NUM 4
#define ParamType unsigned char
ParamType params[PARAM_NUM] = {0, 1, 0, 1};

enum {CMD_MODE, INDEX_MODE, VALUE_MODE};
enum {SET='s', GET='g'};
// Set is ['s', param_idx, param_val(sizeof(ParamType)]
// Get is ['g', param_idx]

// the setup function runs once when you press reset or power the board
void setup() {
  int i;
  // initialize digital pin 13 as an output.
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  while (!Serial) {
    ;
  }
}

char mode = CMD_MODE;
char cmd, idx;
ParamType val;
char valDone;
char dataReady = 0;

void handleCommand(char cmd, char idx, ParamType val)
{
    int i;
    switch (cmd) {
        case GET:
            if (idx < PARAM_NUM) {
              val = params[idx];
            } else {
              val = 0;
            }
            for (i = 0; i < sizeof(ParamType); i ++) {
              Serial.write(val & 0xff);
              val >>= 8;
            }
            break;
        case SET:
            if (idx < PARAM_NUM) {
              params[idx] = val;
            }
            // echo the set command
            Serial.write(0);
            break;
    }
}

void SerialEvent()
{
    int serialInput = 0;
    if (Serial.available()) {
        serialInput = Serial.read();
        switch (mode) {
            case CMD_MODE:
                cmd = serialInput;
                if (cmd == GET || cmd == SET) {
                    mode ++;
                }
                break;
            case INDEX_MODE:
                idx = serialInput;
                if (cmd == GET) {
                    mode = CMD_MODE;
                    dataReady = 1;
                    digitalWrite(13, LOW);
                } else {
                    val = 0;
                    valDone = 0;
                    mode ++;
                }
                break;
            case VALUE_MODE:
                val = val + (((ParamType)serialInput) << (8 * valDone));
                if (++valDone == sizeof(ParamType)) {
                    mode = CMD_MODE;
                    dataReady = 1;
                    digitalWrite(13, HIGH);
                }
                break;
        }
    }
}

// the loop function runs over and over again forever
void loop() {
    SerialEvent();
    if (dataReady) {
      handleCommand(cmd, idx, val);
      dataReady = 0;
    }
}
