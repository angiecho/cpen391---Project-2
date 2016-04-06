#include "mailbox.h"
#include "user.h"
#include <stdlib.h>
#include "control.h"
#include "bluetooth.h"

Mailbox* new_mailbox(){
	return (Mailbox*) malloc(sizeof(Mailbox));
}

void init_mailbox(char receiver, char sender, char* msg, char* key, char* iv, int blk_mult, Mailbox* mailbox){
	mailbox->sender = sender;
	mailbox->receiver = receiver;
	mailbox->msg = msg;
	mailbox->key = key;
	mailbox->iv = iv;
	mailbox->blk_mult = blk_mult;
	mailbox->next = NULL;
}

void check_mailbox(int user_id){
	if(users[user_id].has_mail){
		printf("We have mail\n");
		putCharBluetooth(STX);
		read_mail(users[user_id].mailbox);
		clear_mail(users[user_id].mailbox);
		if(users[user_id].mailbox == NULL){
			users[user_id].has_mail = new_mailbox();
			users[user_id].has_mail = false;
		}
		return;
	}
	printf("We don't have mail\n");
}

void send_mail(char receiver, char sender, char* msg, char* key, char* iv, int blk_mult){
	printf("Sending to mailbox\n");
	if(!users[(int)receiver].has_mail){
		init_mailbox(receiver, sender, msg, key, iv, blk_mult, users[(int)receiver].mailbox);
		return;
	}

	Mailbox tail = *(users[(int)receiver].mailbox);
	while(tail.next != NULL){
		tail = *(tail.next);
	}
	tail.next = new_mailbox();
	tail = *(tail.next);
	init_mailbox(receiver, sender, msg, key, iv, blk_mult, &tail);
}

void read_mail(Mailbox* mailbox){
	for(int j = 0; j < BLK_SIZE*mailbox->blk_mult; j++){
			printf("%d ", mailbox->msg[j]);
		}
	sendMessage(mailbox->receiver, mailbox->sender, mailbox->msg, mailbox->key, mailbox->iv, mailbox->blk_mult);
}

void clear_mail(Mailbox* mailbox){
	Mailbox temp = *mailbox;
	mailbox = temp.next;
	free(&temp);
}
