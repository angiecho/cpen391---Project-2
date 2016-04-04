#include <stdio.h>
#include <stdlib.h>
#include "control.h"
#include "aes.h"
#include "bluetooth.h"
#include "sys/alt_irq.h"
#include <system.h>
#include <assert.h>
#include <unistd.h>

typedef enum {
	start,
	get_header,
	rx_message,
	acknowledge,
	tx_message,
	init
} Stage;

volatile Stage stage;
//volatile unsigned length;
volatile char sender, receiver;
volatile char* msg;
volatile int msg_index;
volatile char bt = 0;

void get_sender_receiver(char ids){
	receiver = ids & 0x0f;
	sender = (ids>>4) & 0x0f;
	printf("Receiver: %d\n", (int)receiver);
	printf("Sender: %d\n", (int)sender);
}

void interruptHandler(void){
	switch(stage){

	case start:
		printf("*start*\n");
		bt = getCharBluetooth();
		printf("ENQ: %d\n", (int)bt);
		if (bt == ENQ){
			printf ("got ENQ\n");
			//do_pop(); TODO: COMMENT OUT THE 3 LINES BELOW WHEN USING KEYBOARD
			key = "abcdefghijklmnop";
			get_key();
			gen_iv();
			stage = get_header;
		}
		break;

	case get_header:
		receiver = getCharBluetooth();
		printf("Receiver: %d\n", (int)receiver);
		sender = getCharBluetooth();
		printf("Sender: %d\n", (int)sender);
		stage = rx_message;

		break;

	case rx_message:
		while(msg_index < 16){
			bt = getCharBluetooth();
			msg[msg_index] = bt;
			msg_index++;
		}

		msg[msg_index] = '\0';
		stage = tx_message;
		//stage = acknowledge; TODO: CHANGE ABOVE LINE TO THIS AFTER
		break;

	case acknowledge:
		putCharBluetooth(ACK);
		usleep(ACK_DURATION);
		bt = getCharBluetooth();
		if(bt == ACK){
			stage = tx_message;
		}
		else{
			stage = init; // TODO: CHANGE THIS TO STORE THE MESSAGE IN THE DATABASE AND NOTIFY THE RECEIVER UPON LOGIN
		}
		break;

	case tx_message:
		sendMessage(msg_index, receiver, sender, msg);
		free(msg);
		free(IV);
		free(key);
		stage = init;
		break;

	case init:
		msg_index = 0;
		key = malloc(sizeof(char)*BLK_SIZE);
		IV = malloc(sizeof(char)*BLK_SIZE);
		msg = malloc(BLK_SIZE+1);
		receiver = 0;
		sender = 0;
		stage = start;
		break;
	}
}

int main(void) {
	init_control();

	stage = init;

	while(1){
		interruptHandler();
	}
//	alt_irq_register(TO_EXTERNAL_BUS_BRIDGE_0_IRQ, NULL, (void *)interruptHandler);
//	printf ("interrupt enabled\n");
//
//	while(1);

	printf("\nDONE\n");
	return 0;
}
