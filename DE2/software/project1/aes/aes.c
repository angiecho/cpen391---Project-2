#include <stdio.h>
#include <string.h>
#include "gps.h"
#include "misc_helpers.h"
#include "control.h"
#include "search.h"
#include "bluetooth.h"
#include "aes.h"

/* get_key will receive a 16 byte key from user
 * input on the touchscreen.
 */
void get_key(char* key){
	for (int i = 0; i < strlen(key); i++){
		putCharBluetooth(key[i]);
	}
	putCharBluetooth(2);
	printf ("got key: %s\n", key);
}

/* gen_iv will generate a 16 char IV based on the
 * GPS coordinates for longitude and latitude.
 */
void gen_iv(void){

	char iv[18];
	iv[17] = '\0';
	long lat, lon;
	read_gps(&lat, &lon);
	long long latXlon = lat * lon * 2;
	for (int i = 0; i < strlen(iv); i++){
		long long temp = latXlon / (10^i);
		iv[i] = getASCII(temp);
		printf("%c, ", iv[i]);
		putCharBluetooth(iv[i]);
	}
	putCharBluetooth(3);
	printf ("got iv: %s", iv);

}

/* getASCII will use an integer value to generate an ASCII value
 * between [33, 126], our valid ASCII characters.
 */
int getASCII(long long c){
	int ascii = c % 127;
	int i = 0;
	while (ascii < 33){
		ascii = (c^i) % 127;
		i++;
	}
	return ascii;
}

//send key and receive message+header from sender
void rcv_message(void){
	bool keyreq = false;
	while (!keyreq){
		keyreq = getCommand();
	}
	s_listen();
}

//send key and send message+header to receiver
void send_message(void){

}
