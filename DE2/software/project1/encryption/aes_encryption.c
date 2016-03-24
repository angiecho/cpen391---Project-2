#include "aes_encryption.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <math.h>
#include <mcrypt.h>

void encrypt(char* buffer, char* key, char* IV){
	MCRYPT td = mcrypt_module_open("rijndael-128", NULL, "cbc", NULL);

	mcrypt_generic_init(td, key, mcrypt_get_key_size(), IV);
	mcrypt_generic(td, buffer, BUFFER_SIZE);
	mcrypt_generic_deinit (td);
	mcrypt_module_close(td);
}

void decrypt(char* buffer, char* key, char* IV){
	MCRYPT td = mcrypt_module_open("rijndael-128", NULL, "cbc", NULL);

	mcrypt_generic_init(td, key, mcrypt_get_key_size(), IV);
	mcrypt_generic(td, buffer, BUFFER_SIZE);
	mcrypt_generic_deinit (td);
	mcrypt_module_close(td);
}

void print_text(char* text, int length){
	for(int i = 0; i < length; i++){
		printf("%c ", text[i]);
	}
	printf("\n");
}
