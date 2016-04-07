#include "user.h"
#include "mailbox.h"
#include <stdlib.h>
#include <stdbool.h>
#include <assert.h>

void init_users(){
	for(int i = 1; i <= MAX_USERS; i++){
		users[i].id = i;
		users[i].mailbox = new_mailbox();
		users[i].mail_count = 0;
		users[i].logged_in = false;
	}
}

int log_in(int user_id){
	if(user_id < 1 || user_id > MAX_USERS){
		return REJECT;
	}
	else if(users[user_id].logged_in){
		return REJECT;
	}
	else{
		users[user_id].logged_in = true;
		return ACCEPT;
	}
}

void log_out(int user_id){
	users[user_id].logged_in = false;
}


