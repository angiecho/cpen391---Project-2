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
	rx_message,
	acknowledge,
} Stage;

volatile Stage stage_list[2];
volatile char sender[2], receiver[2];
volatile char* msg[2];
volatile int msg_index[2];

void init_vars(int index);

void get_sender_receiver(char ids, int curr){
	receiver[curr] = ids & 0x0f;
	sender[curr] = (ids>>4) & 0x0f;
}

void interruptHandler(void){
	char ids;
	char bt;

	while (!(Bluetooth_Status & 0x1) && (!(Bluetooth_RS232_Status & 0x1)));
	int curr = (Bluetooth_Status & 0x1);
	Stage stage = stage_list[curr];

	switch(stage){

	case start:
		printf("*start* %d\n", curr);
		bt = getCharBluetooth(curr);
		msg_index[curr] = 0;
		printf("ENQ: %d\n", (int)bt);
		if (bt == ENQ){
			printf ("got ENQ\n");
			//do_pop(); TODO: COMMENT OUT THE 3 LINES BELOW WHEN USING KEYBOARD
			key[curr] = "abcdefghijklmnop";
			get_key(curr);
			gen_iv(curr);
			stage_list[curr] = rx_message;
		}
		break;

	case rx_message:
		ids = getCharBluetooth(curr);
		printf("msg:\n");
		while(msg_index[curr] < 16){
			bt = getCharBluetooth(curr);
			printf("%d ", bt);
			msg[curr][msg_index[curr]] = bt;
			msg_index[curr]++;
		}
		msg[curr][msg_index[curr]] = '\0';
		printf("\n");
		get_sender_receiver(ids, curr);
		printf("Receiver: %d\n", (int)receiver[curr]);
		printf("Sender: %d\n", (int)sender[curr]);
		sendMessage(msg_index[curr], sender[curr], receiver[curr], msg[curr], curr);
		stage_list[curr] = start;
		//stage = acknowledge; TODO: CHANGE ABOVE LINE TO THIS AFTER
		break;

	case acknowledge:
		putCharBluetooth(ACK, curr);
		usleep(ACK_DURATION);
		bt = getCharBluetooth(curr);
		if(bt == ACK){
			sendMessage(msg_index[curr], sender[curr], receiver[curr], msg[curr], curr);
		}
		else{
			// TODO: CHANGE THIS TO STORE THE MESSAGE IN THE DATABASE AND NOTIFY THE RECEIVER UPON LOGIN
		}
		stage_list[curr] = start;
		break;
	}
}

void init_vars(int index) {
	stage_list[index] = start;
	msg_index[index] = 0;
	msg[index] = malloc(BLK_SIZE+1);
	receiver[index] = 0;
	sender[index] = 0;
}

int main(void) {
	init_control();
	init_vars(LEFT_BT);
	init_vars(RIGHT_BT);

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
