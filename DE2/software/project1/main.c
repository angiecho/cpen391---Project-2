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

volatile Stage stages_list[2];
volatile char sender[2], receiver[2];
volatile char* MSG[2];
volatile char* KEY[2];
volatile char* IV[2];
volatile int msg_index[2];

volatile char bt = 0;
volatile int curr;
volatile char BLK_MULT = 1;

void get_sender_receiver(char ids){
	receiver = ids & 0x0f;
	sender = (ids>>4) & 0x0f;
}

bool confirm_logout(){
	if(getCharBluetooth(curr) == EOT){
		if(getCharBluetooth(curr) == EOT){
			return true;
		}
	}
	return false;
}

void interruptHandler(void){
	char ids;

	switch(stage){

	case start:
		bt = getCharBluetooth(curr);
		if (bt == ENQ){
			//do_pop(); TODO: USE KEYBOARD FOR DEMO
//			while(!key_sent);
//			strcpy(KEY, query_string);
//			gen_iv(IV);
//			send_key(KEY);
//			send_key(IV);
//			key_sent = false;

			KEY = "abcdefghijklmnop";
			send_key(KEY, curr);
			gen_iv(IV);
			send_iv(IV, curr);

			stage = get_header;
		}

		else if(bt == EOT){
			if(confirm_logout()){
				bt = getCharBluetooth(curr);
				stage = logout;
			}
		}
		break;

	case get_header:
		ids = getCharBluetooth(curr);
		get_sender_receiver(ids);
		BLK_MULT = getCharBluetooth(curr);
		stage = rx_message;
		break;

	case rx_message:
		while(msg_index < BLK_SIZE*BLK_MULT){
			bt = getCharBluetooth(curr);
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
		sendMessage(receiver, sender, MSG, KEY, IV, BLK_MULT, curr);
		MSG[0] = "\0";
		stage = init;
		break;

	case mail:
		send_mail(receiver, sender, MSG, KEY, IV, BLK_MULT);
		MSG[0] = "\0";
		stage = init;
		break;

	case init:
		msg_index = 0;
		receiver = 0;
		sender = 0;
		stage = start;
		break;

	case login:
		printf("Log in: ");
		bt = getCharBluetooth(curr);
		printf("%d\n", (int) bt);
		if(log_in(bt)){
			putCharBluetooth(SOH, curr);
			putCharBluetooth(SOH, curr);
			check_mailbox(bt, curr);
			stage = init;
		}
		else{
			putCharBluetooth(NIL, curr);
			putCharBluetooth(NIL, curr);
		}
		break;

	case logout:
		printf("Log out: %d\n", (int) bt);
		log_out(bt);
		stage = login;
		break;
	}
}

int main(void) {
	printf("START\n");
	init_control();
	KEY = malloc(BLK_SIZE);
	IV = malloc(BLK_SIZE);
	MSG = malloc(BLK_SIZE*MAX_MULT+1);

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
