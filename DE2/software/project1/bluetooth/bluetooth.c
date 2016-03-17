#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <bluetooth.h>

void putCharBluetooth(char c){
	while((Bluetooth_Status & 0x02) != 0x02);
	Bluetooth_TxData = c & 0xFF;
}

char getCharBluetooth(){
	while (!(Bluetooth_Status & 0x1));
	return Bluetooth_RxData;
}

void Init_Bluetooth(void){
	Bluetooth_Control = 0x15;
	Bluetooth_Baud = 0x01;
}

void WaitForReadStat(){
	while(!(Bluetooth_Status & 0x01));
}

// Set the bluetooth to command mode.
void commandMode(void){
	printf("Entering Command Mode\n");
	usleep(2000000);
	int i;
	char data[] = "$$$";
	for (i = 0; i < strlen(data); i++){
		putCharBluetooth(data[i]);
	}
	usleep(2000000);
}

// Set the bluetooth to data mode.
void dataMode(void){
	printf("Entering Data Mode\n");
	usleep(2000000);
	char data[] = "---\r\n";
	int i;
	for (i = 0; i < strlen(data); i++){
		putCharBluetooth(data[i]);
	}

	usleep(2000000);
}

void slaveMode(void){
	printf("Entering Slave Mode\n");
	putCharBluetooth('S');
	putCharBluetooth('M');
	putCharBluetooth(',');
	putCharBluetooth('0');
}

// Prepare to give the device a name.
void setName(){
	putCharBluetooth('S');
	putCharBluetooth('N');
	putCharBluetooth(',');
}

// Only interact with current remote addr space
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

// Change the device's name
void changeName(char name[]){
	printf("Changing name\n");
	commandMode();
	setName();

    for(int i = 0; i<strlen(name); i++){
        putCharBluetooth(name[i]);
    }
    usleep(2000000);

	dataMode();
}

// Change the device's password.
void changePassword(char pw[]){
	printf("Changing password\n");
	commandMode();
	setPassword();

    for(int i = 0; i<strlen(pw); i++){
        putCharBluetooth(pw[i]);
    }
    usleep(2000000);

	dataMode();
}

void assignBluetoothCHARLES(void) {
	printf("Test Bluetooth\n");
	Init_Bluetooth();
	printf("Bluetooth Initialized\n");
	char name[] = "CHARLES\r\n";
	char password[] = "0001\r\n";

	changeName(name);
	changePassword(password);
	printf("done\n");
}

void assignBluetoothCHO(void) {
	printf("Test Bluetooth\n");
	Init_Bluetooth();
	printf("Bluetooth Initialized\n");
	char name[] = "CHO\r\n";
	char password[] = "0002\r\n";

	changeName(name);
	changePassword(password);
	printf("done\n");
}

void assignBluetoothCALEB(void) {
	printf("Test Bluetooth\n");
	Init_Bluetooth();
	printf("Bluetooth Initialized\n");
	char name[] = "CALEB\r\n";
	char password[] = "0003\r\n";

	changeName(name);
	changePassword(password);
	printf("done\n");
}

void sendTestData(char word[]){
	for (int i = 0; i < strlen(word); i++){
		putCharBluetooth(word[i]);
	}
}



