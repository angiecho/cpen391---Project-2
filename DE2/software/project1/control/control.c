#include <control.h>
#include <stdlib.h>
#include <assert.h>

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
		*length = 255;
		char* msg = malloc(256);

		for (int i = 0; i < 256; i++) {
			msg[i] = getCharBluetooth();
		}

		msg[256] = '\0';
		return msg;
	}


}

bool sendMessage(unsigned length, char receiver, char sender, char* msg){
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
