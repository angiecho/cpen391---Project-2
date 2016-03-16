#include <stdio.h>
#include "control.h"
#include "bluetooth.h"

int main(void) {
	assignBluetoothCHARLES();
	while(1){
		unsigned length;
		char sender, receiver;
		char* msg = getMessage(&length, &receiver, &sender);
		printf("message from: %c \n", receiver);
		printf("message to: %c \n", sender);
		printf("message: %s \n", msg);
	}
	printf("\nDONE\n");
	return 0;
}
