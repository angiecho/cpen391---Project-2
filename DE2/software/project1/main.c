#include <stdio.h>
#include "control.h"
#include "aes.h"

int main(void) {
	init_control();
	while (1){
		rcv_message();
		send_message();	//not implemented
	}
	return 0;
}