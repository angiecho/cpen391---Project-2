#ifndef CPEN391_PROJECT_2_AES_ENCRYPTION_H
#define CPEN391_PROJECT_2_AES_ENCRYPTION_H

// 128 bit key, IV, block
#define BLOCK_SIZE 16
#define KEY_SIZE 16
#define BUFFER_SIZE 16

//TODO: generate random keys and IVs
char* key = "1234567890qwerty";
char* IV = "uiopasdfghjklzxc";
char* message;
char* buffer;

void encrypt(char* buffer, char* key, char* IV);
void decrypt(char* buffer, char* key, char* IV);
void print_text(char* text, int length);

#endif
