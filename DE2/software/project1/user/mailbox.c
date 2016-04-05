#include "mailbox.h"
#include "user.h"
#include <stdlib.h>
#include "control.h"

Mailbox* new_mailbox(){
	return (Mailbox*) malloc(sizeof(Mailbox));
}

void init_mailbox(char sender, char receiver, char* msg, Mailbox* mailbox){
	mailbox->sender = sender;
	mailbox->receiver = receiver;
	mailbox->msg = msg;
	mailbox->next = NULL;
}

void check_mailbox(int user_id){
	if(users[user_id].has_mail){
		read_mail(users[user_id].mailbox);
		clear_mail(users[user_id].mailbox);
		if(users[user_id].mailbox == NULL){
			users[user_id].has_mail = new_mailbox();
			users[user_id].has_mail = false;
		}
	}
}

void send_mail(char sender, char receiver, char* msg){
	if(!users[(int)receiver].has_mail){
		init_mailbox(sender, receiver, msg, users[(int)receiver].mailbox);
		return;
	}

	Mailbox tail = *(users[(int)receiver].mailbox);
	while(tail.next != NULL){
		tail = *(tail.next);
	}
	tail.next = new_mailbox();
	tail = *(tail.next);
	init_mailbox(sender, receiver, msg, &tail);
}

void read_mail(Mailbox* mailbox){
	sendMessage(mailbox->sender, mailbox->receiver, mailbox->msg);
}

void clear_mail(Mailbox* mailbox){
	Mailbox temp = *mailbox;
	mailbox = temp.next;
	free(&temp);
}
