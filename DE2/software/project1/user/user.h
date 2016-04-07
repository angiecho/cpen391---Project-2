#ifndef USER_H_
#define USER_H_

#include <stdbool.h>
#include "mailbox.h"

#define MAX_USERS 3
#define ACCEPT 1
#define REJECT 0

typedef struct User{
	int id;
	bool logged_in;
	struct Mailbox* mailbox;
	int mail_count;
} User;


User* users;

void init_users();
int log_in(int user_id); // rejects login if user already logged in, otherwise accepts login
void log_out(int user_id);

#endif /* USER_H_ */
