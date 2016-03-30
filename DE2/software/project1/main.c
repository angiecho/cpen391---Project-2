#include <stdio.h>
#include "menu.h"
#include "graphics.h"
#include "touchscreen.h"
#include "control.h"
#include "bluetooth.h"
#include "sys/alt_irq.h"
#include "system.h"
#include <assert.h>

typedef enum {
	start,
	get_length,
	get_message
} Stage;

volatile Stage stage;
volatile unsigned length;
volatile char sender, receiver;
volatile char* msg;
volatile int msg_index;

void get_sender_receiver(void){
	char sender_receiver = getCharBluetooth();
	receiver = sender_receiver & 0x0f;
	sender = (sender_receiver>>4) & 0x0f;
}

void interruptHandler(void){
	switch(stage){

	case start:
		get_sender_receiver();
		stage = get_length;
		break;

	case get_length:
		length = (unsigned)getCharBluetooth();
		assert(length != 0);
		msg = malloc(length+1);
		msg_index = 0;
		stage = get_message;
		break;

	case get_message:

		msg[msg_index] = getCharBluetooth();

		msg_index++;

		if (msg_index == length){
			msg[length] = '\0';
			sendMessage(length, receiver, sender, msg);
			stage = start;
		}

		break;
	}
}

int main(void) {
	int ret;
	Init_Bluetooth();
	printf("Bluetooth initialized!");

	stage = start;

	alt_irq_register(TO_EXTERNAL_BUS_BRIDGE_0_IRQ, NULL, (void *)interruptHandler);
	printf ("interrupt enabled\n");

	while(1);

	printf("\nDONE\n");
	return 0;
}
