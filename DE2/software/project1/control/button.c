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
#include "graph.h"
#include <assert.h>
#include "aes.h"

const static Point NULL_CORNER = {-1,-1};
extern Point curr_image_pos;
extern int zoom_level;
extern int button_iteration;

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
//	else if(id == BACK_BUTT.id){
//		i = id - 21;
//		j = 2;
//		keyboard[id].left = (KB_LEFT+KEY_SIZE) + KEY_SIZE*i;
//		keyboard[id].right = (KB_LEFT+KEY_SIZE*2) + KEY_SIZE*i;
//		keyboard[id].top = KB_TOP + KEY_SIZE*j;
//		keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
//		BACK_BUTT.p = do_back;
//		BACK_BUTT.text = "BACK";
//		return;
//	}
	keyboard[id].left = KB_LEFT + KEY_SIZE*i;
	keyboard[id].right = (KB_LEFT+KEY_SIZE) + KEY_SIZE*i;
	keyboard[id].top = KB_TOP + KEY_SIZE*j;
	keyboard[id].bottom = (KB_TOP + KEY_SIZE) + KEY_SIZE*j;
	keyboard[id].kb_p = do_key;
}



void init_keyboard(){
	keyboard = malloc(sizeof(Button)*(KB_KEYS-1));

	// KB buttons
	for(int i = 0; i < KB_KEYS-1; i++){
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

// Ask for a start and end node and find the best directions
void do_dir(){
	int start_node = get_start_node();
	if (road_only && !vertex_had_road_edge(full_map_graph, start_node)){
		draw_information_box("YOUR CURRENT LOCATION HAS NO ROAD ACCESS. PLEASE TURN OFF THE ROADS BUTTON.");
		DIR_BUTT.prs_p(DIR_BUTT);
		return;
	}
	draw_information_box("PLEASE SELECT DESTINATION");

	//end_node = get_node(full_map_graph);
	if (end_node == -1){
		DIR_BUTT.prs_p(DIR_BUTT);
		//about_screen();
		return;
	}
	if (road_only && !vertex_had_road_edge(full_map_graph, end_node)){
		draw_information_box("YOUR SELECTED LOCATION HAS NO ROAD ACCESS. PLEASE TURN OFF THE ROADS BUTTON.");
		DIR_BUTT.prs_p(DIR_BUTT);
		return;
	}

	load_and_draw_graph_path(full_map_graph, start_node, end_node, road_only, PURPLEPIZZAZZ);

	draw_information_box("HAVE A FUN TRIP!");
	DIR_BUTT.prs_p(DIR_BUTT);
}

//Toggle between zoom in and zoom out
void do_zoom(){
	Point sel = {0,0};
	if (zoom_level == ZOOM_IN){
		zoom_level = ZOOM_OUT;
	}
	else {
		zoom_level = ZOOM_IN;

		do{
			sel = GetPress();
		} while(sel.y > image_height[ZOOM_OUT] || sel.x > image_width[ZOOM_OUT]);

		sel = convert_pnt_to_zoom_in(sel);

		//we pass draw_image in the top left corner, sel should be the centre of the image
		sel.x -= DISPLAY_WIDTH/2;
		sel.y -= DISPLAY_HEIGHT/2;

		if(sel.x < 0)
			sel.x = 0;
		else if(sel.x > (image_width[ZOOM_IN] - DISPLAY_WIDTH))
			sel.x = image_width[ZOOM_IN]  - DISPLAY_WIDTH;

		if(sel.y < 0)
			sel.y = 0;
		else if(sel.y > (image_height[ZOOM_IN] - DISPLAY_HEIGHT))
			sel.y = image_height[ZOOM_IN] - DISPLAY_HEIGHT;
	}

	curr_image_pos = sel;
	draw_full_image();
	//about_screen();
	ZOOM_BUTT.prs_p(ZOOM_BUTT);
	re_draw_path();
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

int get_start_node(){
	int curr_lat, curr_long;
	switch (button_iteration){
		// as shitty as this looks, this is to make testing easier
		case 1:
			get_current_coordinates(&curr_lat, &curr_long);
			break;
		case 0:
			curr_lat = 2504654;
			curr_long = 2656412;
			break;
		case 2:
			curr_lat = 2565916;
			curr_long = 2676995;
			break;
		case 3:
			curr_lat = 2453370;
			curr_long = 2645000;
			break;
	}

	return find_vertex_by_coords(full_map_graph, curr_long, curr_lat)->id;
}

void re_draw_path(){
	if (points != NULL){
		printf("Path is not NULL!");
		draw_graph_path(PURPLEPIZZAZZ);
	}
}

bool draw_graph_path(int colour){
	if (zoom_level == ZOOM_OUT){
		if (!points_equal(prev_zoomed_out_min_corner, NULL_CORNER) && !points_equal(prev_zoomed_out_max_corner, NULL_CORNER)){
			draw_image_segment(prev_zoomed_out_min_corner,  prev_zoomed_out_max_corner);
		}
	}
	else{
		//First check to see if when subtracted from, it's less than 0...
		prev_zoomed_in_min_corner.x = (prev_zoomed_in_min_corner.x - curr_image_pos.x < 0) ? 0 : prev_zoomed_in_min_corner.x - curr_image_pos.x;
		prev_zoomed_in_min_corner.y = (prev_zoomed_in_min_corner.y - curr_image_pos.y < 0) ? 0 : prev_zoomed_in_min_corner.y - curr_image_pos.y;

		//Then check to see if it's greater than the DISPLAY WIDTH
		prev_zoomed_in_min_corner.x = (prev_zoomed_in_min_corner.x > DISPLAY_WIDTH) ? DISPLAY_WIDTH : prev_zoomed_in_min_corner.x;
		prev_zoomed_in_min_corner.y = (prev_zoomed_in_min_corner.y > DISPLAY_HEIGHT) ? DISPLAY_HEIGHT : prev_zoomed_in_min_corner.y;

		//This is all to make sure we don't die a horrible death of sadness and line apocalypse (of negative displays).
		prev_zoomed_in_max_corner.x = (prev_zoomed_in_max_corner.x - curr_image_pos.x < 0) ? 0 : prev_zoomed_in_max_corner.x - curr_image_pos.x;
		prev_zoomed_in_max_corner.y = (prev_zoomed_in_max_corner.y - curr_image_pos.y < 0) ? 0 : prev_zoomed_in_max_corner.y - curr_image_pos.y;
		prev_zoomed_in_max_corner.x = (prev_zoomed_in_max_corner.x > DISPLAY_WIDTH) ? DISPLAY_WIDTH : prev_zoomed_in_max_corner.x;
		prev_zoomed_in_max_corner.y = (prev_zoomed_in_max_corner.y > DISPLAY_HEIGHT) ? DISPLAY_HEIGHT : prev_zoomed_in_max_corner.y;
		if (!points_equal(prev_zoomed_in_min_corner, NULL_CORNER) && !points_equal(prev_zoomed_in_max_corner, NULL_CORNER)){
			draw_image_segment(prev_zoomed_in_min_corner,  prev_zoomed_in_max_corner);
		}
		printf("Corners are now: %d, %d\n%d, %d", prev_zoomed_in_min_corner.x, prev_zoomed_in_min_corner.y, prev_zoomed_in_max_corner.x, prev_zoomed_in_max_corner.y);
	}

	if (points != NULL){
		prev_zoomed_out_min_corner = points->zoomed_out_min_corner;
		prev_zoomed_out_max_corner = points->zoomed_out_max_corner;

		prev_zoomed_in_min_corner = points->zoomed_in_min_corner;
		prev_zoomed_in_max_corner = points->zoomed_in_max_corner;
		if (zoom_level == ZOOM_OUT)
			draw_path(points->zoomed_out_ordered_point_arr, points->actual_size, colour);
		else{
			Point points_arr[points->actual_size];
			for (int i = 0; i < points->actual_size; i++){

				points_arr[i] = NULL_CORNER;
				points_arr[i].x = points->zoomed_in_ordered_point_arr[i].x - curr_image_pos.x;
				points_arr[i].y = points->zoomed_in_ordered_point_arr[i].y - curr_image_pos.y;

				if (points_arr[i].x < 0 && points_arr[i].y < 0){
					printf("Originally... %d is  %d, %d\n", i, points_arr[i].x, points_arr[i].y);
					while (points_arr[i].x < 0 || points_arr[i].y < 0){
						points_arr[i].x+=3;
						points_arr[i].y+=1;
					}
					printf("Now point (%d): %d %d\n", i, points_arr[i].x, points_arr[i].y);
				}

				if (points_arr[i].x >= DISPLAY_WIDTH){
					points_arr[i].x = DISPLAY_WIDTH - 1;
				}
				if (points_arr[i].y >= DISPLAY_HEIGHT){
					points_arr[i].y = DISPLAY_HEIGHT - 1;
				}
				if (points_arr[i].x < 0){
					points_arr[i].x = 0;
				}
				if (points_arr[i].y < 0){
					points_arr[i].y = 0;
				}
			}
			draw_path(points_arr, points->actual_size, colour);
			VLine(600, 0, 480, BLACK);
		}
		//destroy_path_points(points);
		return true;
	}
	return false;
}
