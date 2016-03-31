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
	putCharBluetooth(sender_receiver, receiver);
	putCharBluetooth((char)length, receiver);

	for (int i = 0; i<length; i++){
		putCharBluetooth(msg[i], receiver);
	}
	putCharBluetooth(0, receiver);

	free(msg);

	return true;
}
