#include <stdio.h>
#include "control.h"
#include "bluetooth.h"
#include "sys/alt_irq.h"
#include "system.h"
#include "touchscreen.h"
#include <assert.h>

void get_sender_receiver(char* sender, char* receiver){
	char sender_receiver;
	if (Bluetooth_Status & 0x1) {
		sender_receiver = getCharBluetooth(1);
	} else if (Touchscreen_Status & 0x1) {
		sender_receiver = getCharBluetooth(2);
	} else {
		assert(0);
	}

	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;
}

void interruptHandler(void){
	alt_irq_disable(TO_EXTERNAL_BUS_BRIDGE_0_IRQ);
	char sender, receiver;
	get_sender_receiver(&sender, &receiver);
	unsigned length = (unsigned)getCharBluetooth(sender);
	assert(length != 0);
	volatile char* msg = malloc(length+1);

	for (int i = 0; i < length; i++) {
		msg[i] = getCharBluetooth(sender);
	}
	msg[length] = '\0';

	//TODO switch these
	sendMessage(length, sender, receiver, msg);

	alt_irq_enable(TO_EXTERNAL_BUS_BRIDGE_0_IRQ);
}

int main(void) {
	int ret;
	Init_Bluetooth();
	printf("Bluetooth initialized!");

	alt_irq_register(TO_EXTERNAL_BUS_BRIDGE_0_IRQ, NULL, (void *)interruptHandler);
	printf ("interrupt enabled\n");

	while(1);

	printf("\nDONE\n");
	return 0;
}
