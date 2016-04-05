#ifndef MAILBOX_H_
#define MAILBOX_H_

#include "user.h"

typedef struct Mailbox{
	char* msg;
	char sender;
	char receiver;
	struct Mailbox* next;
} Mailbox;

Mailbox* new_mailbox();
void init_mailbox(char sender, char receiver, char* msg, Mailbox* mailbox);
void check_mailbox(int user_id);
void send_mail(char sender, char receiver, char* msg);
void read_mail(Mailbox* mailbox);
void clear_mail(Mailbox* mailbox);

#endif /* MAILBOX_H_ */
