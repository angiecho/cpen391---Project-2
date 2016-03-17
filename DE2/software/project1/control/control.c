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
		assert(0);
	}
}

char* getMessage2(unsigned* length, char* receiver, char* sender){
	char sender_receiver = getCharBluetooth2();
	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;

	unsigned message_length = (unsigned)getCharBluetooth2();

	//TODO should signal message is continuing instead
	if(message_length != 0){

		*length = message_length;
		char* msg = malloc(message_length+1);

		for (int i = 0; i < message_length; i++) {
			msg[i] = getCharBluetooth2();
		}

		msg[message_length] = '\0';
		return msg;
	} else {
		assert(0);
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

bool sendMessage2(unsigned length, char receiver, char sender, char* msg){
	//TODO determine who to send to
	char sender_receiver = (sender << 4) | receiver;
	putCharBluetooth2(sender_receiver);
	putCharBluetooth2((char)length);

	for (int i = 0; i<length; i++){
		putCharBluetooth2(msg[i]);
	}
	putCharBluetooth2(0);

	free(msg);

	return true;
}
