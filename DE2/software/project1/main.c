#include <stdio.h>
#include <stdlib.h>
#include "control.h"
#include "aes.h"
#include "bluetooth.h"
#include "sys/alt_irq.h"
#include <system.h>
#include <assert.h>
#include <unistd.h>
#include "user.h"
#include "mailbox.h"

typedef enum {
	start,
	get_header,
	rx_message,
	acknowledge,
	tx_message,
	mail,
	init,
	login,
	logout
} Stage;

volatile Stage stage;
volatile char sender, receiver;
volatile char* msg;
volatile int msg_index;
volatile char bt = 0;

void get_sender_receiver(char ids){
	receiver = ids & 0x0f;
	sender = (ids>>4) & 0x0f;
}

bool confirm_logout(){
	if(getCharBluetooth() == EOT){
		if(getCharBluetooth() == EOT){
			return true;
		}
	}
	return false;
}

void interruptHandler(void){
	char ids;

	switch(stage){

	case start:
		printf("*start*\n");

		bt = getCharBluetooth();
		if (bt == ENQ){
			printf ("ENQ\n");
			//do_pop(); TODO: COMMENT OUT THE 3 LINES BELOW WHEN USING KEYBOARD
			key = "abcdefghijklmnop";
			get_key();
			gen_iv();
			stage = rx_message;
		}

		else if(bt == EOT){
			if(confirm_logout()){
				bt = getCharBluetooth();
				stage = logout;
			}
		}
		break;

	case get_header:
		ids = getCharBluetooth();
		get_sender_receiver(ids);
		printf("Receiver: %d\n", (int)receiver);
		printf("Sender: %d\n", (int)sender);
		stage = rx_message;
		break;

	case rx_message:
		while(msg_index < BLK_SIZE){
			bt = getCharBluetooth();
			printf("%d ", bt);
			msg[msg_index] = bt;
			msg_index++;
		}

		msg[msg_index] = '\0';
		printf("\n");

		stage = acknowledge;
		break;

	case acknowledge:
		if (users[(int)receiver].logged_in){
			stage = tx_message;
		}
		else{
			stage = mail;
		}
		break;

	case tx_message:
		//TODO switch this -> it just echoes message back
		sendMessage(sender, receiver, msg);
		//sendMessage(receiver, sender, msg);
		free(msg);
		stage = init;
		break;

	case mail:
		send_mail(sender, receiver, msg);
		users[(int) receiver].has_mail = true;
		free(msg);
		stage = init;
		break;

	case init:
		msg_index = 0;
		key = malloc(BLK_SIZE);
		IV = malloc(BLK_SIZE);
		msg = malloc(BLK_SIZE+1);
		receiver = 0;
		sender = 0;
		stage = start;
		break;

	case login:
		printf("Waiting for login\n");
		bt = getCharBluetooth();
		printf("%d\n", (int)bt);
		if(log_in(bt)){
			//check_mailbox(bt);
			putCharBluetooth(NIL);
			stage = init;
		}
		else{
			putCharBluetooth(NIL);
		}
		break;

	case logout:
		log_out(bt);
		stage = login;
		break;
	}
}

int main(void) {
	init_control();

	stage = login;

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
