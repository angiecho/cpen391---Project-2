#ifndef CONTROL_H_
#define CONTROL_H_

#include "misc_helpers.h"
#include <stdbool.h>

void init_globals();
void init_control();
void s_listen();
void kb_listen();

void load_from_sd();

char* getMessage(unsigned* length, char* receiver, char* sender);
bool sendMessage(unsigned length, char receiver, char sender, char* msg);

#endif /* CONTROL_H_ */
