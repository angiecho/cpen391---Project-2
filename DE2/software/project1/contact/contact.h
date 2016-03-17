#ifndef CPEN391_PROJECT_2_CONTACT_H
#define CPEN391_PROJECT_2_CONTACT_H

#define N_CONTACTS 3

typedef struct contact{
    char* name;
    char* pw;
    int id;
} contact;

contact* contacts;

contact init_contact();
void init_contacts();
void destroy_contacts();



#endif //CPEN391_PROJECT_2_CONTACT_H
