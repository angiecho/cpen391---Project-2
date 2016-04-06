#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <bluetooth.h>
#include <touchscreen.h>

#define ACK 6

void putCharBluetooth(char c){
	while((Bluetooth_Status & 0x02) != 0x02);
	Bluetooth_TxData = c & 0xFF;
	//putCharBluetooth_RS232(c);
}

void putCharBluetooth_RS232(char c){
	while((Bluetooth_RS232_Status & 0x02) != 0x02);
	Bluetooth_RS232_TxData = c & 0xFF;
}

char getCharBluetooth(){
	while (!(Bluetooth_Status & 0x1));
	char data = Bluetooth_RxData;
	putCharBluetooth(ACK);
	putCharBluetooth(ACK);
	putCharBluetooth(ACK);
	return data;
	//return getCharBluetooth_RS232();
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

// Set the bluetooth to command mode.
void commandMode(void){
	usleep(2000000);
	printf("Entering Command Mode\n");
	char data[] = "$$$";
	
	for(int i = 0; i < strlen(data); i++){
		putCharBluetooth(data[i]);
	}
	usleep(2000000);
}

// Set the bluetooth to data mode.
void dataMode(void){
	printf("Entering Data Mode\n");
	char* data = "---\r\n";
	
	for(int i = 0; i < strlen(data); i++){
		putCharBluetooth(data[i]);
	}
}

// Command: Set the bluetooth to be a slave.
void slaveMode(void){
	putCharBluetooth('S');
	putCharBluetooth('M');
	putCharBluetooth(',');
	putCharBluetooth('0');
}

// Command: Prepare to give the device a name.
void setName(){
	putCharBluetooth('S');
	putCharBluetooth('N');
	putCharBluetooth(',');
}

// Command: Only interact with current remote addr space
void enableBond(){
	putCharBluetooth('S');
	putCharBluetooth('X');
	putCharBluetooth(',');
	putCharBluetooth('1');
}

// Interact with everybody
void disableBond(){
	putCharBluetooth('S');
	putCharBluetooth('X');
	putCharBluetooth(',');
	putCharBluetooth('0');
}

// Prepare to give the device a security pin code.
void setPassword(){
	putCharBluetooth('S');
	putCharBluetooth('P');
	putCharBluetooth(',');
}

// Command: Change the device's name.
void changeName(char* name){
	setName();

    for(int i = 0; i<strlen(name); i++){
        putCharBluetooth(name[i]);
    }
}

// Command: Change the device's password.
void changePassword(char* pw){
	setPassword();

    for(int i = 0; i<strlen(pw); i++){
        putCharBluetooth(pw[i]);
    }
}

// Assign the device's with a name and password.
void assignBluetooth(char* name, char* pw){
	commandMode();
	changeName(name);
	changePassword(pw);
	dataMode();
}
