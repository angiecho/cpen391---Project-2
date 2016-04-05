#ifndef CONTROL_H_
#define CONTROL_H_


#include "misc_helpers.h"
#include <stdbool.h>
#include "button.h"

#define BLK_SIZE 16
#define NIL 0
#define SOH 1
#define STX 2
#define ETX 3
#define EOT 4
#define ENQ 5
#define ACK 6
#define BEL 7

char* getMessage(unsigned* length, char* receiver, char* sender);
char* getMessage2(unsigned* length, char* receiver, char* sender);
//frees msg
bool sendMessage(char receiver, char sender, char* msg);
bool sendMessage2(char receiver, char sender, char* msg);

void connection(void);

void init_globals();
void init_control();
void s_listen();
void kb_listen();

void load_from_sd();

#endif /* CONTROL_H_ */
