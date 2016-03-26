#include <stdio.h>
#include "control.h"
#include "bluetooth.h"
#include "aes_encryption.h"

int main(void) {
  	//TODO: need to get key and iv from android
    char* text = "TESTINGWITH16BTS";
    char* key = "1234567890qwerty";
    char* IV = "uiopasdfghjklzxc";
    char* buffer;

    buffer = (char*)calloc(1, BUFFER_SIZE);
    strncpy(buffer, message, BUFFER_SIZE);

    printf("Encryption Test Start\n");
    printf("text: %s\n", text);
    encrypt(buffer, BUFFER_SIZE, IV, key, KEY_SIZE);
    print_cipher(buffer, BUFFER_SIZE);
    decrypt(buffer, KEY_SIZE, IV, key, KEY_SIZE);
    printf("decrypted message: %s\n", buffer);

	printf("\nDONE\n");
	return 0;
}
