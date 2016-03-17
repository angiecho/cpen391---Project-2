#ifndef CONTROL_H_
#define CONTROL_H_

#include <stdbool.h>

char* getMessage(unsigned* length, char* receiver, char* sender);
char* getMessage2(unsigned* length, char* receiver, char* sender);
//frees msg
bool sendMessage(unsigned length, char receiver, char sender, char* msg);
bool sendMessage2(unsigned length, char receiver, char sender, char* msg);

#endif
