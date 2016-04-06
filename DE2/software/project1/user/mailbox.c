#include "mailbox.h"
#include "user.h"
#include <stdlib.h>
#include "control.h"
#include "bluetooth.h"

Mailbox* new_mailbox(){
	return (Mailbox*) malloc(sizeof(Mailbox));
}

void init_mailbox(char sender, char receiver, char* msg, char* key, char* iv, Mailbox* mailbox){
	mailbox->sender = sender;
	mailbox->receiver = receiver;
	mailbox->msg = msg;
	mailbox->key = key;
	mailbox->iv = iv;
	mailbox->next = NULL;
}

void check_mailbox(int user_id){
	if(users[user_id].has_mail){
		putCharBluetooth(STX);
		read_mail(users[user_id].mailbox);
		clear_mail(users[user_id].mailbox);
		if(users[user_id].mailbox == NULL){
			users[user_id].has_mail = new_mailbox();
			users[user_id].has_mail = false;
		}
	}
}

void send_mail(char sender, char receiver, char* msg, char* key, char* iv){
	if(!users[(int)receiver].has_mail){
		init_mailbox(sender, receiver, msg, key, iv, users[(int)receiver].mailbox);
		return;
	}

	Mailbox tail = *(users[(int)receiver].mailbox);
	while(tail.next != NULL){
		tail = *(tail.next);
	}
	tail.next = new_mailbox();
	tail = *(tail.next);
	init_mailbox(sender, receiver, msg, key, iv, &tail);
}

void read_mail(Mailbox* mailbox){
	sendMessage(mailbox->sender, mailbox->receiver, mailbox->msg, mailbox->key, mailbox->iv);
}

void clear_mail(Mailbox* mailbox){
	Mailbox temp = *mailbox;
	mailbox = temp.next;
	free(&temp);
}
