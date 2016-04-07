#include <control.h>
#include <stdlib.h>
#include <assert.h>
#include "menu.h"
#include "gps.h"
#include "control.h"
#include "graphics.h"
#include "touchscreen.h"
#include <math.h>
#include "misc_helpers.h"
#include "button.h"
#include "bluetooth.h"
#include "aes.h"
#include <stdbool.h>

// Initialise components and popup the keyboard
void init_control(){
	Init_GPS();
	Init_Bluetooth();
	init_touch();
	init_keyboard();
}
void kb_listen(){
	while(1){
		Button* butt;
		do{
			Point p_i = GetPress();
			printf("Pressed Coordinates: (%i, %i)\n", p_i.x, p_i.y);
			butt = get_kb_button(p_i);
		}
		while(butt == NULL );
		printf("Button pressed: %c\n", butt->key);
		butt->prs_p(*butt);
		if(butt->id != BACK_BUTT.id && butt->id != ENTER_BUTT.id && butt->id != DEL_BUTT.id && butt->id != ROAD_BUTT.id){
			butt->kb_p(butt->key);
		}

		// We are done with the keyboard upon BACK
		else if(butt->id == BACK_BUTT.id){
			butt->p();
			POP_BUTT.prs_p(POP_BUTT);
			break;
		}

		// We are done with the keyboard upon valid search input
		else if(butt->id == ENTER_BUTT.id){
			if(butt->ent_p(*butt)){}
			if (key_sent == true)
				break;
		}

		else if(butt->id == DEL_BUTT.id || butt->id == ROAD_BUTT.id){
			butt->p();
		}
	}
}

bool sendMessage(unsigned length, char receiver, char sender, char* msg, int curr){
	printf("Sending: \n");
	for (int i = 0; i<strlen(key[curr]); i++){
		printf("%c", key[curr][i]);
		putCharBluetooth(key[curr][i], curr);
	}

	printf("\n");

	for (int i = 0; i<strlen(IV[curr]); i++){
		printf("%c", IV[curr][i]);
		putCharBluetooth(IV[curr][i], curr);
	}
	//keys/ivs are always 16 bits, and placed at front of incoming message,
	//so we do not require the ETX/STX delimiters here
	printf("\n");
	printf("%d\n", sender);
	printf("%d\n", sender);
	char sender_receiver = (sender << 4) | receiver;
	printf("%d\n", sender_receiver);
	putCharBluetooth(sender_receiver, curr);

	for(int i = 0; i<length; i++){
		printf("%d ", msg[i]);
		putCharBluetooth(msg[i], curr);
	}
	printf("\n");
	putCharBluetooth(0, curr);
	putCharBluetooth(0, curr);
	putCharBluetooth(0, curr);

	return true;
}

