#include <stdio.h>
#include <stdlib.h>
#include "button.h"
#include <stdbool.h>
#include <unistd.h>
#include <math.h>
#include "misc_helpers.h"
#include "menu.h"
#include "touchscreen.h"
#include "control.h"
#include "Directions.h"
#include "search.h"
#include "gps.h"
#include <assert.h>
#include "aes.h"

const char KEYS[] = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', '<',
			  	  	'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', '+',
			  	  	'Z', 'X', 'C', 'V', 'B', 'N', 'M', ' ', '>',
			  	  	'!', '@', '#', '$', '%',
			  	  	'a', 'd', 'w', 's', '^'};

// Initialise buttons on the keyboard
void init_kb_button(char key, int id){
	keyboard[id].key = key;
	keyboard[id].id = id;
	keyboard[id].pressed = malloc(sizeof(bool));
	*(keyboard[id].pressed) = false;
	keyboard[id].prs_p = flicker;

	// We can use the id to know where to draw
	int i = 0;
	int j = 0;
	// Del, Enter, Space, and Back are special cases
	if(id < DEL_BUTT.id){
		i = id;
		j = 0;
	}
	else if(id == DEL_BUTT.id){
		i = id;
		j = 0;
		keyboard[id].left = KB_LEFT + KEY_SIZE*i;
		keyboard[id].right = (KB_LEFT+KEY_SIZE) + KEY_SIZE*i;
		keyboard[id].top = KB_TOP + KEY_SIZE*j;
		keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
		DEL_BUTT.p = do_del;
		DEL_BUTT.text = "<-";
		return;
	}
	else if(id < ENTER_BUTT.id){
		i = id - 11;
		j = 1;
	}
	else if(id == ENTER_BUTT.id){
		i = id - 11;
		j = 1;
		keyboard[id].left = KB_LEFT + KEY_SIZE*i;
		keyboard[id].right = (KB_LEFT+KEY_SIZE*2) + KEY_SIZE*i;
		keyboard[id].top = KB_TOP + KEY_SIZE*j;
		keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
		ENTER_BUTT.ent_p = do_enter;
		ENTER_BUTT.text = "ENTER";
		return;
	}
	else if(id >= 21 && id < SPACE_BUTT.id){
		i = id - 21;
		j = 2;
	}
	else if(id == SPACE_BUTT.id){
		i = id - 21;
		j = 2;
		keyboard[id].left = KB_LEFT + KEY_SIZE*i;
		keyboard[id].right = (KB_LEFT+KEY_SIZE*2) + KEY_SIZE*i;
		keyboard[id].top = KB_TOP + KEY_SIZE*j;
		keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
		SPACE_BUTT.kb_p = do_key;
		SPACE_BUTT.text = "SPACE";
		return;
	}
	else if(id == BACK_BUTT.id){
		i = id - 21;
		j = 2;
		keyboard[id].left = (KB_LEFT+KEY_SIZE) + KEY_SIZE*i;
		keyboard[id].right = (KB_LEFT+KEY_SIZE*2) + KEY_SIZE*i;
		keyboard[id].top = KB_TOP + KEY_SIZE*j;
		keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
		BACK_BUTT.p = do_back;
		BACK_BUTT.text = "BACK";
		return;
	}
	keyboard[id].left = KB_LEFT + KEY_SIZE*i;
	keyboard[id].right = (KB_LEFT+KEY_SIZE) + KEY_SIZE*i;
	keyboard[id].top = KB_TOP + KEY_SIZE*j;
	keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
	keyboard[id].kb_p = do_key;
}



void init_keyboard(){
	keyboard = malloc(sizeof(Button)*N_KEYS);

	// KB buttons
	for(int i = 0; i < KB_KEYS; i++){
		init_kb_button(KEYS[i], i);
	}

}

 void destroy_keyboard(Button* keyboard){
	free(keyboard);
}

 // Returns true if the point is inside the button
int falls_inside(Point p, Button b){
	return(falls_between(p.x, b.left, b.right) &&
		   falls_between(p.y, b.top, b.bottom));
}

// Returns the screen button that was pressed and NULL if no valid button was pressed
Button* get_s_button(Point p){
	for(int i = KB_KEYS; i < N_KEYS; i++){
		if(falls_inside(p, keyboard[i])){
			Point p_f = GetRelease();
			printf("Released Coordinates: (%i, %i)\n", p_f.x, p_f.y);

			if(falls_inside(p_f, keyboard[i])){
				return &keyboard[i];
			}
			else {
				printf("Invalid button\n");
				return NULL;
			}
		}
	}

	printf("Invalid button\n");
	return NULL;
}

/* Returns the keyboard button that was pressed and NULL if no valid button was pressed
 * North/south buttons used during search mode to select search matching
 * Road button always active
 */
Button* get_kb_button(Point p){
	for(int i = 0; i < KB_KEYS; i++){
		if(falls_inside(p, keyboard[i])){
			Point p_f = GetRelease();
			printf("Released Coordinates: (%i, %i)\n", p_f.x, p_f.y);

			if(falls_inside(p_f, keyboard[i])){
				return &keyboard[i];
			}
			else {
				printf("Invalid button\n");
				return NULL;
			}
		}
	}

	if(falls_inside(p, NORTH_BUTT)){
		Point p_f = GetRelease();
		printf("Released Coordinates: (%i, %i)\n", p_f.x, p_f.y);

		if(falls_inside(p_f, NORTH_BUTT)){
			return &NORTH_BUTT;
		}
		else{
			printf("Invalid button\n");
			return NULL;
		}
	}
	else if(falls_inside(p, SOUTH_BUTT)){
		Point p_f = GetRelease();
		printf("Released Coordinates: (%i, %i)\n", p_f.x, p_f.y);

		if(falls_inside(p_f, SOUTH_BUTT)){
			return &SOUTH_BUTT;
		}
		else{
			printf("Invalid button\n");
			return NULL;
		}
	}
	else if(falls_inside(p, ROAD_BUTT)){
		Point p_f = GetRelease();
		printf("Released Coordinates: (%i, %i)\n", p_f.x, p_f.y);

		if(falls_inside(p_f, ROAD_BUTT)){
			return &ROAD_BUTT;
		}
		else{
			printf("Invalid button\n");
			return NULL;
		}
	}

	printf("Invalid button\n");
	return NULL;
}

// Pop up the keyboard
void do_pop(){
	pop_screen();
	reset_query();
	kb_listen();
}


// Draws the character on the search bar and updates the search matcher
void do_key(char key){
	add_letter(key);
}

// Deletes the front of the search bar and updates the search matcher
void do_del(){
	del_letter();
}

/* On valid search, find the path to the selected entry from current location. Re-draw the map.
	On invalid search, returns and keep listening for keyboard inputs */
void do_enter(){
	int key_len = qs_length();
	if (key_len != 16){
		printf("Please enter a 16 character key.\n");
		printf("You entered: %d", key_len);
		key_sent = false;
	}
	else {
		key = query_string;
		get_key();
		gen_iv();
		key_sent = true;
	}
}

// Leave search mode and redraw the map
void do_back(){
	reset_query();
}

void flicker(Button b){
	highlight(b);
	usleep(FLICKER_DELAY);
	unhighlight(b);
}

bool is_kb_butt(Button b){
	return(b.id < KB_KEYS);
}

// Pre: b is a keyboard button
bool is_big_kb(Button b){
	return(b.id == ENTER_BUTT.id || b.id == SPACE_BUTT.id || b.id == BACK_BUTT.id);
}

bool is_arrow_butt(Button b){
	return(b.id >= WEST_BUTT.id && b.id <= SOUTH_BUTT.id);
}

bool is_screen_butt(Button b){
	return(!is_kb_butt(b) && !is_arrow_butt(b));
}

// Pre: b has a direction
int get_arrow_dir(Button b){
	return b.dir;
}

// Pre: b has text
char* get_butt_text(Button b){
	return b.text;
}
