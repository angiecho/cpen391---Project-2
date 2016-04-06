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
volatile char* MSG;
volatile char* KEY;
volatile char* IV;
volatile int msg_index;
volatile char bt = 0;
volatile char BLK_MULT = 1;

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
			//do_pop(); TODO: USE KEYBOARD FOR DEMO
//			while(!key_sent);
//			strcpy(KEY, query_string);
//			gen_iv(IV);
//			send_key(KEY);
//			send_key(IV);
//			key_sent = false;

			KEY = "abcdefghijklmnop";
			send_key(KEY);
			gen_iv(IV);
			send_iv(IV);
			printf("KEY/IV sent\n");

			stage = get_header;
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
		BLK_MULT = getCharBluetooth();
		stage = rx_message;
		break;

	case rx_message:
		while(msg_index < BLK_SIZE*BLK_MULT){
			bt = getCharBluetooth();
			printf("%d ", bt);
			MSG[msg_index] = bt;
			msg_index++;
		}

		MSG[msg_index] = '\0';
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
		sendMessage(sender, receiver, MSG, KEY, IV, BLK_MULT); //TODO switch this -> it just echoes message back
		//sendMessage(receiver, sender, MSG);
		free(MSG);
		stage = init;
		break;

	case mail:
		send_mail(receiver, sender, MSG, KEY, IV, BLK_MULT);
		users[(int) receiver].has_mail = true;
		printf("Is the mail okay?\n");
		view_message(receiver);
		free(MSG);
		stage = init;
		break;

	case init:
		msg_index = 0;
		KEY = malloc(BLK_SIZE);
		IV = malloc(BLK_SIZE);
		MSG = malloc(BLK_SIZE*MAX_MULT+1);
		receiver = 0;
		sender = 0;
		stage = start;
		break;

	case login:
		printf("Logging in with user_id: ");
		bt = getCharBluetooth();
		printf("%d\n", (int) bt);
		if(log_in(bt)){
			putCharBluetooth(SOH);
			putCharBluetooth(SOH);
			check_mailbox(bt);
			stage = init;
		}
		else{
			putCharBluetooth(NIL);
			putCharBluetooth(NIL);
		}
		break;

	case logout:
		printf("Logging out with user_id: %d\n", (int) bt);
		log_out(bt);
		stage = login;
		break;
	}
}

int main(void) {
	printf("START\n");
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
