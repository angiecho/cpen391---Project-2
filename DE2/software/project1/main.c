#include <stdio.h>
#include "control.h"
#include "bluetooth.h"

int main(void) {
	Init_Bluetooth();
	while(1){
		unsigned length;
		char sender, receiver;
		//char* msg = getMessage(&length, &receiver, &sender);
		char* msg = getMessage2(&length, &receiver, &sender);
		printf("message from: %d \n", receiver);
		printf("message to: %d \n", sender);
		printf("message length: %d \n", length);
		printf("message: %s \n", msg);
		//sendMessage(length, receiver, sender, msg);
		sendMessage2(length, receiver, sender, msg);
	}
	printf("\nDONE\n");
	return 0;
}
