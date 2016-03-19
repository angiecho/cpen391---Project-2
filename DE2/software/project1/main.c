#include <stdio.h>
#include "control.h"
#include "bluetooth.h"

int main(void) {
	Init_Bluetooth();

	while(1){
		unsigned length;
		char sender, receiver;
		char* msg = getMessage2(&length, &receiver, &sender);
		printf("message from: %d \n", receiver);
		printf("message to: %d \n", sender);
		printf("message length: %d \n", length);
		printf("message: %s \n", msg);
		sendMessage(length, receiver, sender, msg);
		char* msg2 = getMessage(&length, &receiver, &sender);
		sendMessage2(length, receiver, sender, msg2);
	}
	printf("\nDONE\n");
	return 0;
}
