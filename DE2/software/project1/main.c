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
#include "search.h"

typedef enum {
	start,
	get_header,
	rx_message,
	login
} Stage;

volatile Stage stages_list[2];
volatile char sender[2], receiver[2];
volatile char* MSG_list[2];
volatile char* KEY_list[2];
volatile char* IV_list[2];
volatile int msg_index_list[2];
volatile int curr;

volatile char BLK_MULT_list[2];

void get_sender_receiver(char ids){
	receiver[curr] = ids & 0x0f;
	sender[curr] = (ids>>4) & 0x0f;
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
	char bt = 0;

	while (!(Bluetooth_Status & 0x1) && (!(Bluetooth_RS232_Status & 0x1)));
	int curr = (Bluetooth_Status & 0x1);

	Stage stage = stages_list[curr];
	int msg_index = msg_index_list[curr];
	char BLK_MULT = BLK_MULT_list[curr];

	switch(stage){

	case start:
		msg_index = 0;
		bt = getCharBluetooth(curr);
		if (bt == ENQ){
			//do_pop(); TODO: USE KEYBOARD FOR DEMO
//			while(!key_sent);
//			strcpy(KEY, query_string);
//			gen_iv(IV);
//			send_key(KEY);
//			send_key(IV);
//			key_sent = false;

			KEY_list[curr] = "abcdefghijklmnop";
			send_key(KEY_list[curr], curr);
			gen_iv(IV_list[curr]);
			send_iv(IV_list[curr], curr);

			stage = get_header;
		}

		else if(bt == EOT){
			if(confirm_logout()){
				bt = getCharBluetooth(curr);
				log_out(bt);
				stage = login;
			}
		}
		break;

	case get_header:
		ids = getCharBluetooth(curr);
		get_sender_receiver(ids);
		BLK_MULT = getCharBluetooth(curr);
		printf("blk_mult header = %d\n", BLK_MULT);
		stage = rx_message;
		break;

	case rx_message:
		printf("rx :\n");
		printf("blk_mult header= %d\n", BLK_MULT);
		while(msg_index < BLK_SIZE*BLK_MULT){
			bt = getCharBluetooth(curr);
			printf("%d ", bt);
			MSG_list[curr][msg_index] = bt;
			msg_index++;
		}

		MSG_list[curr][msg_index] = '\0';
		printf("\n");

		printf("ack stage\n");
		if (!users[(int)receiver[curr]].logged_in){
			printf("Key send message: %s\n", KEY_list[curr]);
			sendMessage(sender[curr],receiver[curr],  MSG_list[curr], KEY_list[curr], IV_list[curr], BLK_MULT, curr);
			MSG_list[curr][0] = "\0";
			stage = start;
		}
		else{
			send_mail(receiver[curr], sender[curr], MSG_list[curr], KEY_list[curr], IV_list[curr], BLK_MULT);
			MSG_list[curr][0] = "\0";
			stage = start;
		}
		break;

	case login:
		printf("Log in: ");
		bt = getCharBluetooth(curr);
		printf("%d\n", (int) bt);
		if(log_in(bt)){
			putCharBluetooth(SOH, curr);
			putCharBluetooth(SOH, curr);
			check_mailbox(bt, curr);
			stage = start;
		}
		else{
			putCharBluetooth(NIL, curr);
			putCharBluetooth(NIL, curr);
		}
		break;
	}

	stages_list[curr] = stage;
	msg_index_list[curr] = msg_index;
	BLK_MULT_list[curr] = BLK_MULT;
}

void init_vars(int index){
	KEY_list[index] = malloc(BLK_SIZE);
	IV_list[index] = malloc(BLK_SIZE);
	MSG_list[index] = malloc(BLK_SIZE*MAX_MULT+1);
	BLK_MULT_list[index] = 1;
	stages_list[index] = login;
}

int main(void) {
	init_control();
	//test_touch();
	do_pop();
	char* KEY = malloc(BLK_SIZE);
			while(!key_sent);
			strcpy(KEY, query_string);
			for(int i = 0; i < BLK_SIZE; i++){
				printf("%c", KEY[i]);
			}
	clear_screen(WHITE);
//	init_vars(LEFT_BT);
//	init_vars(RIGHT_BT);
//
//	printf("starting\n");
//
//	while(1){
//		interruptHandler();
//	}
//	alt_irq_register(TO_EXTERNAL_BUS_BRIDGE_0_IRQ, NULL, (void *)interruptHandler);
//	printf ("interrupt enabled\n");
//
//	while(1);

	printf("\nDONE\n");
	return 0;
}
