#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("rancidbacon.com",
		     "BackgroundUsbDemo",
		     "Background USB accessory demo",
		     "0.1",
		     "http://rancidbacon.com/",
		     "0000000000000001");


void setup() {
  acc.powerOn();
}

void loop() {
  if (acc.isConnected()) {  
  }
  delay(10);
}
