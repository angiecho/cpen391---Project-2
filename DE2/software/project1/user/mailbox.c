#include "mailbox.h"
#include "user.h"
#include <stdlib.h>
#include "control.h"
#include "bluetooth.h"
#include <string.h>
#include "aes.h"

Mailbox* new_mailbox(){
	return (Mailbox*)malloc(MAILBOX_SIZE);
}

void init_mailbox(char receiver, char sender, char* msg, char* key, char* iv, int blk_mult, Mailbox* mailbox){
	mailbox->sender = sender;
	mailbox->receiver = receiver;
	mailbox->blk_mult = blk_mult;

	mailbox->msg = malloc(BLK_SIZE*blk_mult);
	mailbox->key = malloc(BLK_SIZE);
	mailbox->iv = malloc(BLK_SIZE);
	strcpy(mailbox->msg, msg);
	strcpy(mailbox->key, key);
	strcpy(mailbox->iv, iv);
}

void print_message(int user_id){
	printf("Message:\n");
	int mail_index = users[user_id].mail_count - 1;
	for(int j = 0; j < BLK_SIZE*users[user_id].mailbox[mail_index].blk_mult; j++){
		printf("%d ", users[user_id].mailbox[mail_index].msg[j]);
	}
	printf("\n");
}

void check_mailbox(int user_id){
	int mail_index = -1;
	while(users[user_id].mail_count > 0){
		printf("User %d has %d message(s)\n", user_id, users[user_id].mail_count);
		putCharBluetooth(STX);
		mail_index++;
		read_mail(users[user_id].mailbox[mail_index]);
		users[user_id].mail_count--;
	}
	users[user_id].mail_count = 0;
	printf("User %d has %d message(s)\n", user_id, users[user_id].mail_count);
}

void send_mail(char receiver, char sender, char* msg, char* key, char* iv, int blk_mult){
	printf("Sending to mailbox\n");
	users[(int)receiver].mail_count++;
	int mail_index = users[(int)receiver].mail_count - 1;

	init_mailbox(receiver, sender, msg, key, iv, blk_mult, &(users[(int)receiver].mailbox[mail_index]));

	printf("User %d has %d message(s)\n",(int)receiver,users[(int)receiver].mail_count);
}

void read_mail(Mailbox mailbox){
	sendMessage(mailbox.receiver, mailbox.sender, mailbox.msg, mailbox.key, mailbox.iv, mailbox.blk_mult);
	clear_mail(mailbox.receiver);
}

void clear_mail(int user_id){
	free(users[user_id].mailbox);
	users[user_id].mailbox = new_mailbox();
}
