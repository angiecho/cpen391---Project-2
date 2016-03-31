#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <bluetooth.h>
#include <touchscreen.h>
#include <assert.h>

char getChar1();
char getChar2();
void putChar1(char c);
void putChar2(char c);

void putChar1(char c){
	while((Bluetooth_Status & 0x02) != 0x02);
	Bluetooth_TxData = c & 0xFF;
}

void putChar2(char c){
	while((Touchscreen_Status & 0x02) != 0x02);
	Touchscreen_TxData = c & 0xFF;
}

void putCharBluetooth(char c, char id){
	if (id == 2) {
		putChar1(c);
	} else if (id == 1) {
		putChar2(c);
	} else {
		assert(0);
	}
}


char getChar1(){
	while (!(Bluetooth_Status & 0x1));
	return Bluetooth_RxData;
}

char getChar2(){
	while (!(Bluetooth_Status & 0x1));
	return Bluetooth_RxData;
}

char getCharBluetooth(char id){
	if (id == 1) {
		return getChar1();
	} else if (id == 2) {
		return getChar2();
	} else {
		assert(0);
	}
}

void Init_Bluetooth(void){
	Bluetooth_Control = 0x95;
	Touchscreen_Control = 0x15;
	Bluetooth_Baud = 0x01;
	Touchscreen_Baud = 0x01;
}

void WaitForReadStat(){
	while(!(Bluetooth_Status & 0x01));
}

//// Set the bluetooth to command mode.
//void commandMode(void){
//	usleep(2000000);
//	printf("Entering Command Mode\n");
//	char data[] = "$$$";
//
//	for(int i = 0; i < strlen(data); i++){
//		putCharBluetooth(data[i]);
//	}
//	usleep(2000000);
//}
//
//// Set the bluetooth to data mode.
//void dataMode(void){
//	printf("Entering Data Mode\n");
//	char* data = "---\r\n";
//
//	for(int i = 0; i < strlen(data); i++){
//		putCharBluetooth(data[i]);
//	}
//}
//
//// Command: Set the bluetooth to be a slave.
//void slaveMode(void){
//	putCharBluetooth('S');
//	putCharBluetooth('M');
//	putCharBluetooth(',');
//	putCharBluetooth('0');
//}
//
//// Command: Prepare to give the device a name.
//void setName(){
//	putCharBluetooth('S');
//	putCharBluetooth('N');
//	putCharBluetooth(',');
//}
//
//// Command: Only interact with current remote addr space
//void enableBond(){
//	putCharBluetooth('S');
//	putCharBluetooth('X');
//	putCharBluetooth(',');
//	putCharBluetooth('1');
//}
//
//// Interact with everybody
//void disableBond(){
//	putCharBluetooth('S');
//	putCharBluetooth('X');
//	putCharBluetooth(',');
//	putCharBluetooth('0');
//}
//
//// Prepare to give the device a security pin code.
//void setPassword(){
//	putCharBluetooth('S');
//	putCharBluetooth('P');
//	putCharBluetooth(',');
//}
//
//// Command: Change the device's name.
//void changeName(char* name){
//	setName();
//
//    for(int i = 0; i<strlen(name); i++){
//        putCharBluetooth(name[i]);
//    }
//}
//
//// Command: Change the device's password.
//void changePassword(char* pw){
//	setPassword();
//
//    for(int i = 0; i<strlen(pw); i++){
//        putCharBluetooth(pw[i]);
//    }
//}
//
//// Assign the device's with a name and password.
//void assignBluetooth(char* name, char* pw){
//	commandMode();
//	changeName(name);
//	changePassword(pw);
//	dataMode();
//}
//
//

