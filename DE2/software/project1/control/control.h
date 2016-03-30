#ifndef CONTROL_H_
#define CONTROL_H_

#include <stdbool.h>

char* getMessage(unsigned* length, char* receiver, char* sender);
bool sendMessage(unsigned length, char receiver, char sender, char* msg);
bool echoMessage(unsigned length, char receiver, char sender, char* msg);

#endif
