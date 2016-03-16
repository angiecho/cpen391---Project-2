#include <stdlib.h>

char* getMessage(unsigned* length, char* receiver, char* sender){
	char sender_receiver = getCharBluetooth();
	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;

	unsigned message_length = (unsigned)getCharBluetooth();
	//TODO should signal message is continuing instead
	assert(message_length != 0);

	// need to include sender, receiver, and null terminator
	*length = message_length+1;
	char* msg = malloc(*length);

	for (int i = 0; i < message_length; i++) {
		msg[i] = getCharBluetooth();
	}

	msg[message_length] = '\0';

	return msg;
}
