#include <stdio.h>
#include "graphics.h"
#include "touchscreen.h"
#include "gps.h"
#include "bluetooth.h"

int main(void) {
	printf("Starting...\n");
	Init_Bluetooth();
	dataMode();
	printf("Connection successful!\n");
	while(1){
		char c = getCharBluetooth();
		if (c){
			printf("%c", c);
		}
	}
	printf("\nDONE\n");
	return 0;
}
