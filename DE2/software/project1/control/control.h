#ifndef CONTROL_H_
#define CONTROL_H_


#include "misc_helpers.h"
#include <stdbool.h>
#include "button.h"

#define BLK_SIZE 16
#define MAX_MULT 64
#define NIL 0
#define SOH 1
#define STX 2
#define ETX 3
#define EOT 4
#define ENQ 5
#define ACK 6
#define BEL 7

//frees msg
bool sendMessage(char receiver, char sender, char* msg,
				char* key, char* iv, int blk_mult, int curr);

void connection(void);

void init_globals();
void init_control();
void s_listen();
void kb_listen();

void load_from_sd();

#endif /* CONTROL_H_ */
