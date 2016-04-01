#include <stdio.h>
#include <stdlib.h>
#include "graph.h"
#include "menu.h"
#include "gps.h"
#include "control.h"
#include "graphics.h"
#include "touchscreen.h"
#include <math.h>
#include "load_node.h"
#include "misc_helpers.h"
#include "button.h"
#include "bluetooth.h"
#include "aes.h"
#include <stdbool.h>

const static Point NULL_CORNER = {-1,-1};
extern Point curr_image_pos, prev_zoomed_in_min_corner, prev_zoomed_in_max_corner, prev_zoomed_out_min_corner, prev_zoomed_out_max_corner;
extern int zoom_level;
extern bool road_only;
extern int end_node;
extern path_points* points;

// Initialise components and popup the keyboard
void init_control(){
	init_globals();
	Init_GPS();
	Init_Bluetooth();
	init_touch();
	init_keyboard();
}

void init_globals(){
	prev_zoomed_in_min_corner = NULL_CORNER;
	prev_zoomed_in_max_corner = NULL_CORNER;
	prev_zoomed_out_min_corner = NULL_CORNER;
	prev_zoomed_out_max_corner = NULL_CORNER;
	zoom_level = ZOOM_OUT;
	Point p = {0,0};
	curr_image_pos = p;
	road_only = false;
	end_node = 0;
	points = NULL;
}

// Get the node from where we pressed
int get_node(graph* graph){

	int node_id = -1;

	while(node_id == -1){
		int a,b,c,d;
		Point p_i, p_f;
		do{
			p_i = GetPress();
			a = p_i.x;
			b = p_i.y;
			printf("Pressed Coordinates: (%i, %i)\n", a, b);

			p_f = GetRelease();
			c = p_f.x;
			d = p_f.y;
			printf("Released Coordinates: (%i, %i)\n", c, d);

		} while(sqrt(pow((c-a),2) + pow((d-b),2)) > RADIUS); //check for valid press & release

		if(falls_inside(p_f, DIR_BUTT)) {
			return -1;
		}

		node_id = get_valid_vertex(graph, p_f);
		if (node_id == -1) {
			draw_information_box("NOT A VALID MAP POINT.");
		}
		if (road_only && !vertex_had_road_edge(full_map_graph, node_id)){
			draw_information_box("THIS MAP POINT HAS NO ROAD ACCESS. PLEASE TURN OFF THE ROADS ONLY BUTTON.");
			node_id = -1;
		}
	}
	return node_id;
}

// Get a node ensuring it has info
char* get_node_info(graph* graph){
	int node_id = -1;
	char* info = malloc(sizeof(char)*500);

	while(node_id == -1){
		int a,b,c,d;
		Point p_i, p_f;
		do{
			p_i = GetPress();
			a = p_i.x;
			b = p_i.y;
			printf("Pressed Coordinates: (%i, %i)\n", a, b);

			p_f = GetRelease();
			c = p_f.x;
			d = p_f.y;
			printf("Released Coordinates: (%i, %i)\n", c, d);

		} while(sqrt(pow((c-a),2) + pow((d-b),2)) > RADIUS); //check for valid press & release

		node_id = get_valid_vertex(graph, p_f);
		if (node_id == -1) {
			if(falls_inside(p_f, INFO_BUTT)) {
				return NULL;
			}
			draw_information_box("NOT A VALID MAP POINT.");
		}
		else{
			vertex* v = get_vertex(full_map_graph, node_id);
			strcpy(info, v->info);

			if(strlen(info) < 1  || info == '\0'){
				node_id = -1;
				draw_information_box("NO INFORMATION AVAILABLE FOR THIS MAP POINT.");
			}
		}
	}
	return info;
}

/* Returns the node if we pressed a point sufficiently close to the node. Assumption: Each node has
   a finite metric in relation to every other node, i.e there is a maximum of one node sufficiently close.*/
//TODO optimize this shit yo
int get_valid_vertex(graph* graph, Point p){
	for(int i = 0; i<graph->num_vertices; i++) {
		vertex v = *graph->vertices[i];
		Point vertex_p = NULL_CORNER;
		if (zoom_level == ZOOM_OUT){
			vertex_p = get_vertex_xy(&v, false);
		}
		else {
			vertex_p = get_vertex_xy(&v, true);
			vertex_p.x = vertex_p.x - curr_image_pos.x;
			vertex_p.y = vertex_p.y - curr_image_pos.y;
		}
		if (sqrt((pow((vertex_p.x-p.x),2) + pow((vertex_p.y-p.y),2))) <= RADIUS ){
			return v.id;
		}
	}
	return -1;
}

// Listen for screen button presses
void s_listen(){
	while(1){
		Button* butt;
		do{
			Point p_i = GetPress();
			printf("Pressed Coordinates: (%i, %i)\n", p_i.x, p_i.y);
			butt = get_s_button(p_i);
		}
		while(butt == NULL);
		printf("Button pressed: %c\n", butt->key);
		butt->prs_p(*butt);
		butt->p();
	}
}

// Listen for keyboard + NORTH/SOUTH + ROAD button presses
void kb_listen(){
	while(1){
		Button* butt;
		do{
			Point p_i = GetPress();
			printf("Pressed Coordinates: (%i, %i)\n", p_i.x, p_i.y);
			butt = get_kb_button(p_i);
		}
		while(butt == NULL );
		printf("Button pressed: %c\n", butt->key);
		butt->prs_p(*butt);
		if(butt->id != BACK_BUTT.id && butt->id != ENTER_BUTT.id && butt->id != DEL_BUTT.id && butt->id != ROAD_BUTT.id){
			butt->kb_p(butt->key);
		}

		// We are done with the keyboard upon valid search input
		else if(butt->id == ENTER_BUTT.id){
			if(butt->ent_p(*butt)){}
			if (key_sent == true)
				break;
		}

		else if(butt->id == DEL_BUTT.id){
			butt->p();
		}
	}
}
char* getMessage(unsigned* length, char* receiver, char* sender){
	char sender_receiver = getCharBluetooth();
	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;

	unsigned message_length = (unsigned)getCharBluetooth();

	//TODO should signal message is continuing instead
	if(message_length != 0){

		*length = message_length;
		char* msg = malloc(message_length+1);

		for (int i = 0; i < message_length; i++) {
			msg[i] = getCharBluetooth();
		}

		msg[message_length] = '\0';
		return msg;
	} else {
		//assert(0);
	}
}

char* getMessage2(unsigned* length, char* receiver, char* sender){
	char sender_receiver = getCharBluetooth2();
	*receiver = sender_receiver & 0x0f;
	*sender = (sender_receiver>>4) & 0x0f;

	unsigned message_length = (unsigned)getCharBluetooth2();

	//TODO should signal message is continuing instead
	if(message_length != 0){

		*length = message_length;
		char* msg = malloc(message_length+1);

		for (int i = 0; i < message_length; i++) {
			msg[i] = getCharBluetooth2();
		}

		msg[message_length] = '\0';
		return msg;
	} else {
		//assert(0);
	}
}

bool sendMessage(unsigned length, char receiver, char sender, char* msg){
	printf("Sending: ");
	for (int i = 0; i<strlen(key); i++){
		printf("%c", key[i]);
		putCharBluetooth(key[i]);
	}
	printf("\n");
	putCharBluetooth(STX);

	for (int i = 0; i<strlen(IV); i++){
		printf("%c", IV[i]);
		putCharBluetooth(IV[i]);
	}
	printf("\n");
	putCharBluetooth(ETX);

	char sender_receiver = (sender << 4) | receiver;
	putCharBluetooth(sender_receiver);
	//putCharBluetooth((char)length);

	for(int i = 0; i<length; i++){
		printf("%d ", msg[i]);
		putCharBluetooth(msg[i]);
	}
	printf("\n");
	putCharBluetooth(0);

	return true;
}

bool sendMessage2(unsigned length, char receiver, char sender, char* msg){
	for (int i = 0; i<strlen(key); i++){
		putCharBluetooth2(key[i]);
	}
	putCharBluetooth2(STX);

	for (int i = 0; i<strlen(IV); i++){
		putCharBluetooth2(IV[i]);
	}
	putCharBluetooth2(ETX);

	char sender_receiver = (sender << 4) | receiver;
	putCharBluetooth2(sender_receiver);
	putCharBluetooth2((char)length);

	for (int i = 0; i<length; i++){
		putCharBluetooth2(msg[i]);
	}
	putCharBluetooth2(0);

	return true;
}
