#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define USE_LCD 1
#define USE_SHIFT_LCD 0

#ifdef USE_LCD
#if USE_SHIFT_LCD
#include <ShiftLCD.h>
ShiftLCD lcd(6, A2, A3);
#else
#include <LiquidCrystal.h>

// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(A0, A1, A2, A3, A4, A5);
#endif
#endif


AndroidAccessory acc("rancidbacon.com",
		     "BackgroundUsbDemo",
		     "Background USB Demo accessory",
		     "0.1",
		     "http://rancidbacon.com/",
		     "0000000000000001");


void setup() {
  Serial.begin(9600);

#ifdef USE_LCD
  // set up the LCD's number of columns and rows: 
  lcd.begin(16, 2);
#endif

  acc.powerOn();  
}

void loop() {
  byte msg[1];
  
  if (acc.isConnected()) {
    byte len = acc.read(msg, sizeof(msg), 1);
    if (len == 1) {
#ifdef USE_LCD
    if ((msg[0] == 0x01) || (msg[0] == 0x02)) {
      lcd.setCursor(0, msg[0]-1);
    } else 
#endif
      if (msg[0] == '\n') {
#ifdef USE_LCD
        lcd.clear();      
#else
        Serial.println();
#endif        
      } else {
#ifdef USE_LCD      
        lcd.print(msg[0]);      
#else
        Serial.print(msg[0]);
#endif
      }
    }
  } else {
#ifdef USE_LCD    
    lcd.clear();
#endif    
  }
  
  delay(10);
}
