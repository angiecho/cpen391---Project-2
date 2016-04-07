#include "mailbox.h"
#include "user.h"
#include <stdlib.h>
#include "control.h"
#include "bluetooth.h"
#include <string.h>
#include "aes.h"

Mailbox* new_mailbox(){
	return (Mailbox*)malloc(MAX_MAILBOX);
}

void init_mailbox(char receiver, char sender, char* msg, char* key, char* iv, int blk_mult){
	int mail_index = users[(int)receiver].mail_count - 1;

	users[(int)receiver].mailbox[mail_index].sender = sender;
	users[(int)receiver].mailbox[mail_index].receiver = receiver;
	users[(int)receiver].mailbox[mail_index].blk_mult = blk_mult;

	users[(int)receiver].mailbox[mail_index].msg = malloc(BLK_SIZE*MAX_MULT);
	users[(int)receiver].mailbox[mail_index].key = malloc(BLK_SIZE);
	users[(int)receiver].mailbox[mail_index].iv = malloc(BLK_SIZE);
	strcpy(users[(int)receiver].mailbox[mail_index].msg, msg);
	strcpy(users[(int)receiver].mailbox[mail_index].key, key);
	strcpy(users[(int)receiver].mailbox[mail_index].iv, iv);
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
	printf("SENDING TO MAILBOX\n");
	users[(int)receiver].mail_count++;

	init_mailbox(receiver, sender, msg, key, iv, blk_mult);

	print_message(receiver);
	printf("User %d has %d message(s)\n",(int)receiver,users[(int)receiver].mail_count);
}

void read_mail(Mailbox mailbox){
	sendMessage(mailbox.receiver, mailbox.sender, mailbox.msg, mailbox.key, mailbox.iv, mailbox.blk_mult);
	//clear_mail(mailbox.receiver);
}

void clear_mail(int user_id){
	free(users[user_id].mailbox->msg);
	free(users[user_id].mailbox->key);
	free(users[user_id].mailbox->iv);
}
