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
#include "user.h"

// Initialise components and popup the keyboard
void init_control(){
	Init_GPS();
	Init_Bluetooth();
	init_touch();
	init_keyboard();
	init_users();
}
void kb_listen(){
	char *keylen = malloc(sizeof(char)*30);
	char kblen[3];
	while(1){
		Button* butt;
		do{
			Point p_i = GetPress();
			printf("Pressed Coordinates: (%i, %i)\n", p_i.x, p_i.y);
			butt = get_kb_button(p_i);
			itoa(qs_length(),kblen);
			strcpy(keylen,"You have entered: \n");
			strcat(keylen,kblen);
			strcat(keylen, " out of 16 Characters");

			draw_information_box(keylen);
		}
		while(butt == NULL );
		printf("Button pressed: %c\n", butt->key);
		butt->prs_p(*butt);



		if(butt->id != ENTER_BUTT.id && butt->id != DEL_BUTT.id){
			butt->kb_p(butt->key);
		}

//		// We are done with the keyboard upon BACK
//		else if(butt->id == BACK_BUTT.id){
//			butt->p();
//			POP_BUTT.prs_p(POP_BUTT);
//			break;
//		}

		// We are done with the keyboard upon valid search input
		else if(butt->id == ENTER_BUTT.id){
			butt->ent_p(*butt);
			if (key_sent == true)
				break;
		}

		else if(butt->id == DEL_BUTT.id){
			butt->p();
		}
	}
}

bool sendMessage(char receiver, char sender, char* msg,
					char* key, char* iv, int blk_mult, int curr){
	printf("SENDING TO ANDROID: \n");
	for (int i = 0; i<BLK_SIZE; i++){
		printf("%c", key[i]);
		putCharBluetooth(key[i], curr);
	}

	printf("\n");

	for (int i = 0; i<BLK_SIZE; i++){
		printf("%c", iv[i]);
		putCharBluetooth(iv[i], curr);
	}
	//keys/ivs are always 16 bits, and placed at front of incoming message
	printf("\n");

	char sender_receiver = (sender << 4) | receiver;
	putCharBluetooth(sender_receiver, curr);

	for(int i = 0; i<BLK_SIZE*blk_mult; i++){
		printf("%d ", msg[i]);
		putCharBluetooth(msg[i], curr);
	}
	printf("\n");
	putCharBluetooth(0, curr);
	putCharBluetooth(0, curr);
	putCharBluetooth(0, curr);

	return true;
}
