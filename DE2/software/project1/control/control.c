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
			if(butt->ent_p(*butt)){
				POP_BUTT.prs_p(POP_BUTT);
				break;
			}
		}

		else if(butt->id == DEL_BUTT.id || butt->id == ROAD_BUTT.id){
			butt->p();
		}
	}
}

char* getMessage(unsigned* length, char* receiver, char* sender){
	char sender_receiver = getCharBluetooth();
	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;

	unsigned message_length = (unsigned)getCharBluetooth();

	//TODO should signal message is continuing instead
	if(message_length != 0){

		*length = message_length;
		char* msg = malloc(message_length+1);

		for (int i = 0; i < message_length; i++) {
			msg[i] = getCharBluetooth();
		}

		msg[message_length] = '\0';
		return msg;
	} else {
		//assert(0);
	}
}

bool sendMessage(unsigned length, char receiver, char sender, char* msg){
	printf("message from: %d \n", sender);
	printf("message to: %d \n", receiver);
	printf("message length: %d \n", length);
	printf("message: %s \n", msg);
	//TODO determine who to send to

	char sender_receiver = (sender << 4) | receiver;
	putCharBluetooth(sender_receiver);
	putCharBluetooth((char)length);

	for (int i = 0; i<length; i++){
		putCharBluetooth(msg[i]);
	}
	putCharBluetooth(0);

	free(msg);

	return true;
}
