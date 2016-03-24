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

	/*
	//TODO: generate random keys and IVs
    	char* key = "1234567890qwerty";
    	char* IV = "uiopasdfghjklzxc";
    	char* message = "01110TESTING TESTING";
    	char* buffer;

    	buffer = (char*)calloc(1, BUFFER_SIZE);
    	strncpy(buffer, message, BUFFER_SIZE);

    	printf("Encryption Test Start\n");
    	printf("message: %s\n", message);
    	encrypt(buffer, BUFFER_SIZE, IV, key, KEY_SIZE);
    	printf("encrypted cipher: %s\n", buffer); //print_text(buffer, BUFFER_SIZE);
    	decrypt(buffer, KEY_SIZE, IV, key, KEY_SIZE);
    	printf("decrypted message: %s\n", buffer);
	return 0;
	*/
}
