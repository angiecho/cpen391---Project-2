#ifndef SEARCH_H_
#define SEARCH_H_

#include <string.h>
#include <graph.h>
#include <stdbool.h>

//TODO change constants
#define MAX_CHAR 26 - 1 // n - 1 because of index; Change n to be the longest node name
#define X 40
#define Y 235
#define INCR 20 // Change this so that MAX_CHAR letters fits

#define MAX_MATCHES 10
#define SEARCH_THRESHHOLD 3 // Both of these can be changed later

char query_string[MAX_CHAR];

// fcns for query string
void reset_query();
int qs_length();
bool has_space();
void add_letter(char letter);
void del_letter();
void draw_word();

#endif /* SEARCH_H_ */
