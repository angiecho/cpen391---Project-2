#ifndef CONTROL_H_
#define CONTROL_H_

#define MAX_LENGTH 140
#define SOH 1
#define STX 2
#define ETX 3
#define EOT 4
#define ENQ 5
#define ACK 6
#define BEL 7
#include "graph.h"
#include "button.h"
#include <stdbool.h>

char* getMessage(unsigned* length, char* receiver, char* sender);
char* getMessage2(unsigned* length, char* receiver, char* sender);
//frees msg
bool sendMessage(unsigned length, char receiver, char sender, char* msg);
bool sendMessage2(unsigned length, char receiver, char sender, char* msg);

void connection(void);

void init_globals();
void init_control();
void s_listen();
void kb_listen();

int get_node(graph* graph);
char* get_node_info(graph* graph);
int get_valid_vertex(graph* graph, Point p);

#endif /* CONTROL_H_ */
