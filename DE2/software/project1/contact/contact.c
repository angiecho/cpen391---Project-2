#include "bluetooth.h"
#include "contact.h"

const char* contact_names[] = {"CHARLES", "CHO", "CALEB"};
const char* passwords[] = {"0001", "0002", "0003"};

contact init_contact(char* name, char* pw, int id){
    contact c;
    c.name = name;
    c.pw = pw;
    c.id = id;
    return c;
}

void init_contacts(){
	contacts = malloc(sizeof(contact)*N_CONTACTS);

	for(int i = 0; i < N_CONTACTS; i++){
		contacts[i] = init_contact(contact_names[i], passwords[i], i+1);
	}
}

void assign_contacts(){
   for(int i = 0; i < N_CONTACTS; i++){
   		assignBluetooth(contact[i].name, contacts[i].pw);
   }
}

destroy_contacts(){
    free(contacts);
}
