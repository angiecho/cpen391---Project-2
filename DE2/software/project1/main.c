#include <stdio.h>
#include "control.h"
#include "bluetooth.h"
#include "touchscreen.h"
#include "sys/alt_irq.h"

void interruptPrint(void){
 	printf ("interrupt enabled\n");
 	printf("%s\n", getCharBluetooth());
 	//Setting the Timeout back to 0 so that the timer
 	//can run again. Offset is 0
 	//IOWR_16DIRECT(TO_EXTERNAL_BUS_BRIDGE_0_BASE, 0, 0x0);
 }

int main(void) {
	int ret;
	Init_Bluetooth();
	printf("Bluetooth initialized!");
	while(1){
<<<<<<< dd837e292857954780715d70d4f89c2e1e48cf71
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
=======
//	ret = alt_irq_register(TO_EXTERNAL_BUS_BRIDGE_0_IRQ, NULL, (void *)interruptPrint);
//	if (ret != 0){
//		printf("Couldn't register irq\n");
//	}
	//printf("start wait\n");
	//set our timer to start, be continuous, and enable it's IRQ line. offset is 1
	//IOWR_16DIRECT(TIMER_1_BASE, 1, 0x7);
	unsigned length;
	char sender, receiver;
	char* msg = NULL;
	msg = getMessage(&length, &receiver, &sender);
	printf("message from: %d \n", receiver);
	printf("message to: %d \n", sender);
	printf("message length: %d \n", length);
	printf("message: %s \n", msg);
	sendMessage(length, receiver, sender, msg);
>>>>>>> initial commit
	}
//		char* msg = getMessage2(&length, &receiver, &sender);

//		//sendMessage(length, receiver, sender, msg);
//		sendMessage2(length, receiver, sender, msg);
//	}
	while(1);
	printf("\nDONE\n");
	return 0;
}
