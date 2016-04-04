#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "gps.h"
#include "misc_helpers.h"
#include "control.h"
#include "search.h"
#include "bluetooth.h"
#include "aes.h"
#include "menu.h"
#include <stdbool.h>

/* get_key will receive a 16 byte key from user
 * input on the touchscreen.
 */
void get_key(){
	for (int i = 0; i < 16; i++){
		putCharBluetooth(key[i]);
	}
	putCharBluetooth(STX);
	printf ("Key: %s\n", key);
}

/* gen_iv will generate a 16 char IV based on the
 * GPS coordinates for longitude and latitude.
 */
void gen_iv(void){
	char iv[17];
	iv[16] = '\0';
	long lat, lon;
	read_gps(&lat, &lon);
	long long latXlon = lat * lon * 2;
	printf("IV: ");
	for (int i = 0; i < strlen(iv); i++){
		long long temp = latXlon / (10^i);
		iv[i] = getASCII(temp);
		printf("%c", iv[i]);
		putCharBluetooth(iv[i]);
	}
	printf("\n");
	putCharBluetooth(ETX);
	IV = malloc(sizeof(char) * strlen(iv));
	strcpy(IV,iv);
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
