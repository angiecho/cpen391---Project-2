#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <assert.h>
#include <bluetooth.h>
#include <touchscreen.h>

#define ACK 6

char getCharBluetooth_RIGHT();
void putCharBluetooth_RIGHT(char c);

void putCharBluetooth(char c, int curr){
	assert(curr == LEFT_BT || curr == RIGHT_BT);
	if (curr == LEFT_BT) {
		putCharBluetooth_RS232(c);
	} else if (curr == RIGHT_BT) {
		putCharBluetooth_RIGHT(c);
	} else {
		assert(0);
	}
}

void putCharBluetooth_RIGHT(char c){
	while((Bluetooth_Status & 0x02) != 0x02);
	Bluetooth_TxData = c & 0xFF;
}

void putCharBluetooth_RS232(char c){
	while((Bluetooth_RS232_Status & 0x02) != 0x02);
	Bluetooth_RS232_TxData = c & 0xFF;
}

char getCharBluetooth(int curr){
	assert(curr == LEFT_BT || curr == RIGHT_BT);
	if (curr == LEFT_BT) {
		return getCharBluetooth_RS232();
	} else if (curr == RIGHT_BT) {
		return getCharBluetooth_RIGHT();
	} else {
		assert(0);
	}
}

char getCharBluetooth_RIGHT(){
	while (!(Bluetooth_Status & 0x1));
	char data = Bluetooth_RxData;
	putCharBluetooth_RIGHT(ACK);
	putCharBluetooth_RIGHT(ACK);
	putCharBluetooth_RIGHT(ACK);
	return data;
}

char getCharBluetooth_RS232(){
	while (!(Bluetooth_RS232_Status & 0x1));
	char data = Bluetooth_RS232_RxData;
	putCharBluetooth_RS232(ACK);
	putCharBluetooth_RS232(ACK);
	putCharBluetooth_RS232(ACK);
	return data;
}

void Init_Bluetooth(void){
	Bluetooth_Control = 0x95;
	Bluetooth_Baud = 0x01;
	Bluetooth_RS232_Control = 0x95;
	Bluetooth_RS232_Baud = 0x01;
}

void WaitForReadStat(){
	while(!(Bluetooth_Status & 0x01));
}
