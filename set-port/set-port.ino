#define PARAM_NUM 6
#define NSTEPPERS 3
#define ParamType unsigned char
#define NPULSE    5653
#define DELAY_US  100

enum {DISABLED, LEFT, RIGHT};
enum {CMD_MODE, INDEX_MODE, VALUE_MODE};
enum {SET='s', GET='g'};
// Set is ['s', param_idx, param_val(sizeof(ParamType)]
// Get is ['g', param_idx]

ParamType params[PARAM_NUM] = {0, 2, 1, 1, 0, 1};
int sensors[PARAM_NUM] = {37, 40, 41, 38, 18, 19};

const int pulse  [NSTEPPERS] = {26, 4, 56};
const int direct [NSTEPPERS] = {27, 54, 60};
const int enable [NSTEPPERS] = {25, 5, 55};

int steppers[NSTEPPERS] = {DISABLED, DISABLED, DISABLED};

// the setup function runs once when you press reset or power the board
void setup() {
  for (int i = 0; i < PARAM_NUM; i ++) {
      pinMode(sensors[i], INPUT);
  }

  for (int i = 0; i < NSTEPPERS; i ++) {
      pinMode(pulse[i], OUTPUT);
      pinMode(direct[i], OUTPUT);
      pinMode(enable[i], OUTPUT);
  }

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
            } else if (idx >= PARAM_NUM && idx < PARAM_NUM + NSTEPPERS) {
              // Special ports for tuning the gate stepper motors
              if (val) {
                openGate(idx - PARAM_NUM, true);
              } else {
                openGate(idx - PARAM_NUM, false);
              }
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
                }
                break;
        }
    }
}

void openGate(int index, boolean toRight) 
{
    digitalWrite(enable[index], LOW);
    delay(50);

    if (toRight) {
        digitalWrite(direct[index], HIGH);
    } else {
        digitalWrite(direct[index], LOW);
    }

    for (long i=0; i<NPULSE; i++) {
        digitalWrite(pulse[index], HIGH);
        delayMicroseconds(DELAY_US);
        digitalWrite(pulse[index], LOW);
        delayMicroseconds(DELAY_US);
    }

    digitalWrite(enable[index], HIGH);
}

boolean checkSensors(int start)
{
    int param = DISABLED;

    if (params[start]) {
        if (digitalRead(sensors[start])) {
            delay(2000);
            param = params[start];
        }
    } else if (params[start+1]) {
        if (digitalRead(sensors[start+1])) {
            delay(2000);
            param = params[start+1];
        }
    } else {
        // no sensor is enabled, no work to do
        return true;
    }

    if (param == LEFT) {
        steppers[start/2] = param;
        openGate(start/2, false);
        if (start == 0) {
            while (!checkSensors(2)) {
                ;
            }
        }
    } else if (param == RIGHT) {
        steppers[start/2] = param;
        openGate(start/2, true);
        if (start == 0) {
            while (!checkSensors(4)) {
                ;
            }
        }
    }

    if (param == DISABLED)
        return false;  // no sensor is triggered
    else
        return true;
}

void resetSteppers()
{
    for (int i = 0; i < NSTEPPERS; i ++) {
        if (steppers[i] == LEFT) {
            openGate(i, true);
        } else if (steppers[i] == RIGHT) {
            openGate(i, false);
        }
    }
}

// the loop function runs over and over again forever
void loop() 
{
    SerialEvent();
    if (dataReady) {
      handleCommand(cmd, idx, val);
      dataReady = 0;
    } else {
      if (checkSensors(0)) {
          delay(1000);
          resetSteppers();
      }
    }
}
