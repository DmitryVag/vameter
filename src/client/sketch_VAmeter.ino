#include <string.h>

#define PIN_V_UP_1   2
#define PIN_V_DOWN   3
#define PIN_RESISTOR 4
#define PIN_V_UP_2   5

#define PIN_FULL_V   0
#define PIN_RESIST_V 1
#define PIN_DIODE_V  2
#define PIN_CAP_V    3

#define RESISTANCE_1 220.0f
#define RESISTANCE_0 830.0f

#define N_POINTS     50
#define N_STATES     5
#define N_VOLTAGES   10

#define DELAY        5
#define MAX_DELAY    1000

#define VOLTAGE_CONV (5.0f/1024.0f)

struct vState {
  int vDown;
  int vUp1;
  int vUp2;
};

struct point {
  float elemV;
  float elemI;
  float capacitorV;
};


struct vState states [] = {
  {HIGH, LOW, LOW}, {LOW, LOW, LOW}, {LOW, HIGH, LOW}, {LOW, LOW, HIGH}, {LOW, HIGH, HIGH}
};

struct point points [N_POINTS];
char* package [10];
double vStep = 0.05;

void swichState(struct vState state) {
  digitalWrite(PIN_V_DOWN, state.vDown);
  digitalWrite(PIN_V_UP_1, state.vUp1);
  digitalWrite(PIN_V_UP_2, state.vUp2);
}

struct point processPoint(int resistorSignal) {
  struct point p;

  float fullV = analogRead(PIN_FULL_V) * VOLTAGE_CONV;
  float resistV = analogRead(PIN_RESIST_V) * VOLTAGE_CONV;
  float diodeV = analogRead(PIN_DIODE_V) * VOLTAGE_CONV;
  float capV = analogRead(PIN_CAP_V) * VOLTAGE_CONV;

  float  resistance = resistorSignal == LOW ? RESISTANCE_0 : RESISTANCE_1;
  
  p.elemV = fullV - resistV;
  p.elemI = (resistV - diodeV) / resistance;
  p.capacitorV = 

  return p;
}

void printPoint(struct point p) {
  Serial.print(p.elemV * 100);
  Serial.print("\t");
  Serial.println(p.elemI * 100000);
}

void setup() {
  pinMode(PIN_V_UP_1, OUTPUT);
  pinMode(PIN_V_UP_2, OUTPUT);
  pinMode(PIN_V_DOWN, OUTPUT);
  pinMode(PIN_RESISTOR, OUTPUT);

  digitalWrite(PIN_RESISTOR, HIGH);
  Serial.begin(9600);
  Serial.println("Available");

  for (int i = 0; i < 10; i++) {
    package[i] = 0;
  }
}

void loadPackage(char** package, char splitter, char finish) {
  for (int i = 0; i < 10; i++) {
    //    Serial.println((int)package[i]);
    if (package[i] == 0) {
      package[i] = new char[20];
      //      Serial.print("to: ");
      //      Serial.println((int)package[i]);
    }
    package[i][0] = '\0';
  }
  int i = 0;
  int j = 0;
  while (true) {
    while (Serial.available() == 0) {}
    char c = Serial.read();
    //    Serial.print("Got ");
    //    Serial.println(c);
    if (c == finish) {
      //      Serial.println("f");
      package[i][j++] = '\0';
      break;
    } else if (c == splitter) {
      //      Serial.println("l");
      package[i][j++] = '\0';
      i++;
      j = 0;
    } else {
      package[i][j++] = c;
    }
  }
}

void readData(int targetState, int resistorSignal) {
  long lastTime = millis();
  int nPoints = 1;

  digitalWrite(PIN_RESISTOR, resistorSignal);
  swichState(states[targetState]);

  points[0] = processPoint(resistorSignal);

  while (true) {
    struct point p = processPoint(resistorSignal);
    if (abs(p.elemV - points[nPoints - 1].elemV) > vStep) {
      points[nPoints++] = p;
      lastTime = millis();
      if (nPoints == N_POINTS) {
        break;
      }
    }
    if (millis()-lastTime > MAX_DELAY) {
      break;
    }
  }


  Serial.print("Got ");
  Serial.println(nPoints);
  for (int i = 0; i < nPoints; i++) {
    printPoint(points[i]);
  }
}

int getInteger (char* s) {
  int res = 0;
  for (char* i = s; (*(i + 1)) != '\0'; i++) {
    res *= 10;
    res += (*i) - '0';
  }
  return res;
}

void loop() {
  if (Serial.available() > 0) {
    loadPackage(package, ' ', '\n');
//    for (int i = 0; i < 10; i++)
//      Serial.println(package[i]);

    if (!strcmp(package[0], "Get")) {

      int targetState = package[1][0] - '0';
      int resistorSignal = package[2][0] - '0';

      readData(targetState, resistorSignal);

    } else if (!strcmp(package[0], "Wait")) {

      int theState = package[1][0] - '0';
      int theDelay = getInteger(package[2]);

      swichState(states[theState]);
      delay(theDelay);

    } else if (!strcmp(package[0], "Set")) {
      if (strcmp(package[1], "vStep")) {
        vStep = getInteger(package[2]) / 1000.0;
      }
    } else {
      Serial.print("Not supported command ");
      Serial.println(package[0]);
    }


    Serial.println("Available");
  }
}
