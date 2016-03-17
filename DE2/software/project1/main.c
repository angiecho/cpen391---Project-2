#include <stdio.h>

#include "control.h"
#include "bluetooth.h"

int main(void) {
	Init_Bluetooth();
	printf("Bluetooth initialized\n");
//	while (1){
//		getCharBluetooth();
//	}

	while(1){
		unsigned length;
		char sender, receiver;
		char* msg = getMessage(&length, &receiver, &sender);
		printf("message length: %d\n", length);
		printf("message from: %d \n", receiver);
		printf("message to: %d \n", sender);
		printf("message: %s \n", msg);
		putCharBluetooth("A");
		putCharBluetooth(10);
		//sendMessage(length, receiver, sender, msg);
	}
	printf("\nDONE\n");
	return 0;
}
