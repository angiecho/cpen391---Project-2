#include "misc_helpers.h"
#include "FontSize.h"
#include "menu.h"
#include "Colours.h"
#include "Directions.h"
#include <stdio.h>
#include <string.h>
#include <touchscreen.h>
#include <altera_up_sd_card_avalon_interface.h>
#include "Directions.h"
#include "search.h"

//Text box is left aligned and has text wrapping
void draw_text_box(Point topLeft, int width, int height, int borderWidth,
		int borderColour, int fillColour, int textColour, char* text, FontSize fontSize){
	Point topRight = {topLeft.x + width, topLeft.y};
	Point bottomLeft = {topLeft.x, topLeft.y + height};
	Point bottomRight = {topLeft.x + width, topLeft.y + height};

	draw_filled_rectangle_border(topLeft, topRight, bottomLeft, bottomRight, fillColour, borderColour, borderWidth);
	int initialX = topLeft.x + 10; //give 10 pixels margin
	int initialY = topLeft.y + 10; //May make this an adjustable variable

	int fontSizePixelsWidth = get_font_width(fontSize);
	int fontSizePixelsHeight = get_font_height(fontSize);

	char* temp_text;
	strcpy(temp_text, text);

	char * token = strtok(temp_text, " ");

	while (token != NULL){
		int stringLength = (int) strlen(token);
		if (initialX + (stringLength * (fontSizePixelsWidth + 1)) > topRight.x - 10){
			initialX = topLeft.x + 10;
			initialY = initialY + fontSizePixelsHeight + 1;
		}
		for (int x = 0; x < stringLength; x++){
			if (token[x] == '\n'){
				initialX = topLeft.x + 4;
				initialY = initialY + fontSizePixelsHeight + 1;
			}
			draw_font(initialX, initialY, textColour, textColour, (int)token[x], 0, fontSize);
			initialX = initialX + fontSizePixelsWidth + 1;
		}
		draw_font(initialX, initialY, textColour, textColour, (int)" ", 0, fontSize);
		initialX = initialX + fontSizePixelsWidth + 1;
		token = strtok(NULL, " ");
	}

}

//draw the info box on the RHS of the touchscreen
void draw_information_box(char* text){
	Point point8;
	point8.x = 175;
	point8.y = 125;
	//draw_text_box(point8, BOX_WIDTH, 50, 1, BLACK, WHITE, BLACK, text, SMALL);
	draw_text_box(point8, 165, 50, 1, WHITE, WHITE, BLACK, text, SMALL);
}

//button is center aligned and has no text wrapping
void draw_button(Point topLeft, int width, int height, int borderWidth,
		int borderColour, int fillColour, int textColour, char* text, FontSize fontSize){
	int textLength = (int) strlen(text);

	Point topRight = {topLeft.x + width, topLeft.y};
	Point bottomLeft = {topLeft.x, topLeft.y + height};
	Point bottomRight = {topLeft.x + width, topLeft.y + height};

	draw_filled_rectangle_border(topLeft, topRight, bottomLeft, bottomRight, fillColour, borderColour, borderWidth);

	int fontSizePixelsWidth = get_font_width(fontSize);
	int fontSizePixelsHeight = get_font_height(fontSize);

	int initialX = topLeft.x + width/2 - (textLength * (fontSizePixelsWidth + 1))/2;
	int initialY = topLeft.y + height/2 - fontSizePixelsHeight/2;

	for (int x = 0; x < textLength; x++){
		draw_font(initialX, initialY, textColour, textColour, (int)text[x], 0, fontSize);
		initialX = initialX + fontSizePixelsWidth + 1;
	}
}

//menu is buttons from top down
void draw_menu(Point leftCorner, int width, int height, int borderWidth,
		int borderColour, int fillColour, int textColour, FontSize fontSize, char** menuText){
	int x = 0;
	while (menuText[x] != ""){
		draw_button(leftCorner, width, height, borderWidth, borderColour, fillColour, textColour, menuText[x], fontSize);
		leftCorner.y+= height;
		x++;
	}
}

void draw_keyboard(Point leftCorner, int size){
	//qwertyuiop <-
	//asdfghjkl ENTER
	//zxcvbnm SPACE BACK

	// These are the letters
	char topRow[] = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', '\0'};
	char homeRow[] = {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', '\0'};
	char bottomRow[] = {'Z', 'X', 'C', 'V', 'B', 'N', 'M', '\0'};

	// Draw top row
	int x = 0;
	Point initialLeftCorner = {leftCorner.x, leftCorner.y};
	while(topRow[x] != '\0'){
		char* c = "A";
		strncpy(c, &topRow[x], 1);
		draw_button(leftCorner, size, size, 1, BLACK, WHITE, BLACK, c, MEDIUM);
		leftCorner.x += size;
		x++;
	}
	// Draw <-
	draw_button(leftCorner, size, size, 1, BLACK, WHITE, BLACK, "<", MEDIUM);

	// Draw home row
	x = 0;
	leftCorner.x = initialLeftCorner.x;
	leftCorner.y = initialLeftCorner.y + size;
	while(homeRow[x] != '\0'){
		char* c = "A";
		strncpy(c, &homeRow[x], 1);
		draw_button(leftCorner, size, size, 1, BLACK, WHITE, BLACK, c, MEDIUM);
		leftCorner.x += size;
		x++;
	}

	// Draw ENTER
	draw_button(leftCorner, 2*size, size, 1, BLACK, WHITE, BLACK, "ENTER", MEDIUM);

	// Draw bottom row
	leftCorner.x = initialLeftCorner.x;
	leftCorner.y = initialLeftCorner.y + 2*size;
	x = 0;
	while(bottomRow[x] != '\0'){
		char* c = "A";
		strncpy(c, &bottomRow[x], 1);
		draw_button(leftCorner, size, size, 1, BLACK, WHITE, BLACK, c, MEDIUM);
		leftCorner.x += size;
		x++;
	}

	// Draw SPACE and BACK
	draw_button(leftCorner, 2*size, size, 1, BLACK, WHITE, BLACK, "SPACE", MEDIUM);
	leftCorner.x += 2*size;
	draw_button(leftCorner, 2*size, size, 1, BLACK, WHITE, BLACK, "BACK", MEDIUM);
	return;
}

void draw_arrow(Point topLeft, int width, int height, int borderWidth, int borderColour, int fillColour, Direction direction, int invert){

	Point topRight = {topLeft.x + width, topLeft.y};
	Point bottomLeft = {topLeft.x, topLeft.y + height};
	Point bottomRight = {topLeft.x + width, topLeft.y + height};
	draw_filled_rectangle_border(topLeft, topRight, bottomLeft, bottomRight, fillColour, borderColour, borderWidth);

	Point cornerOne = {0, 0};
	Point cornerTwo = {0, 0};
	Point cornerThree = {0, 0};

	if (direction == UP){
		cornerOne.x = topLeft.x + width/2;
		cornerOne.y = topLeft.y + height/3;
		cornerTwo.x = topLeft.x + width/3;
		cornerTwo.y = topLeft.y + 2*height/3;
		cornerThree.x = topLeft.x + 2*width/3;
		cornerThree.y = topLeft.y + 2*height/3;
	}
	else if (direction == RIGHT){
		cornerOne.x = topLeft.x + width/3;
		cornerOne.y = topLeft.y + height/3;
		cornerTwo.x = topLeft.x + width/3;
		cornerTwo.y =  topLeft.y + 2*height/3;
		cornerThree.x = topLeft.x + 2*width/3;
		cornerThree.y = topLeft.y + height/2;
	}
	else if (direction == DOWN){
		cornerOne.x = topLeft.x + width/3;
		cornerOne.y = topLeft.y + height/3;
		cornerTwo.x = topLeft.x + 2*width/3;
		cornerTwo.y =  topLeft.y + height/3;
		cornerThree.x = topLeft.x + width/2;
		cornerThree.y = topLeft.y + 2*height/3;
	}
	else{
		cornerOne.x = topLeft.x + width/3;
		cornerOne.y = topLeft.y + height/2;
		cornerTwo.x = topLeft.x + 2*width/3;
		cornerTwo.y =  topLeft.y + height/3;
		cornerThree.x = topLeft.x + 2*width/3;
		cornerThree.y = topLeft.y + 2*height/3;
	}
	if(!invert){
		draw_filled_triangle(cornerOne, cornerTwo, cornerThree, BLACK);
	}
	else{
		draw_filled_triangle(cornerOne, cornerTwo, cornerThree, WHITE);
	}
}

void draw_arrows(){
	// bounded in (500-800, 200-330); midway pts: (650, 275)
	Point LPOINT = {WL, WT};
	Point RPOINT = {EL, ET};
	Point UPOINT = {NL, NT};
	Point DPOINT = {SL, ST};

	draw_arrow(LPOINT, ARROW_SIZE, ARROW_SIZE, 1, BLACK, WHITE, LEFT, 0);

	draw_arrow(RPOINT, ARROW_SIZE, ARROW_SIZE, 1, BLACK, WHITE, RIGHT, 0);

	draw_arrow(UPOINT, ARROW_SIZE, ARROW_SIZE, 1, BLACK, WHITE, UP, 0);

	draw_arrow(DPOINT, ARROW_SIZE, ARROW_SIZE, 1, BLACK, WHITE, DOWN, 0);
}

void init_screen(){
		clear_screen(WHITE);

		// These points define these button locations
		Point searchP = {MAP_WIDTH, BOX_HEIGHT + ARR_BUTT_HEIGHT};
		Point roadP = {MAP_WIDTH+SBUTT_WIDTH, BOX_HEIGHT + ARR_BUTT_HEIGHT};
		Point arrowsP = {MAP_WIDTH, 200};

		char* arrowsText[] = {" ", ""};
		char* searchText[] = {"SEARCH", "INFO", "ZOOM", ""};
		char* roadText[] = {"ROADS", "DIRECTIONS", "ABOUT", ""};


		draw_menu(arrowsP, BOX_WIDTH, ARR_BUTT_HEIGHT, 1 , BLACK, WHITE, BLACK, SMALL, arrowsText);
		draw_menu(searchP, SBUTT_WIDTH, SBUTT_HEIGHT, 1 , BLACK, WHITE, BLACK, SMALL, searchText);
		draw_menu(roadP, SBUTT_WIDTH, SBUTT_HEIGHT, 1, BLACK, WHITE, BLACK, SMALL, roadText);

		draw_arrows();
}

void zoom_screen(){
//	char* name = "PIC.BMP";
//	Point point = {500, 0};
//	load_image(point, name, 330, 300);
	draw_information_box("ZOOMING");
}

// draw the pop up keyboard on the LHS of the screen
void pop_screen(){

	char* t[] = {" ", ""};

	Point topLeft = {0,0};
	Point topRight = {800,0};
	Point botLeft = {0,480};
	Point botRight = {800, 480};
	draw_filled_rectangle(topLeft, topRight, botLeft, botRight, BLACK);
	draw_information_box("ENTER A 16 BYTE KEY!");
	Point kb_p = {KB_LEFT, KB_TOP};
	Point search_p = {ORIGIN, SEARCH_TOP};
	draw_menu(search_p, MAP_WIDTH, SEARCH_HEIGHT, 2, BLACK, WHITE, BLACK, SMALL, t);
	draw_keyboard(kb_p, KEY_SIZE);
}

void highlight(Button b){
	Point p = {b.left, b.top};
	if(is_kb_butt(b)){
		if(is_big_kb(b)){
			draw_button(p, KEY_SIZE*2, KEY_SIZE, 1, WHITE, BLACK, WHITE, b.text, MEDIUM);
		}
		else{
			char* c = "A";
			strncpy(c, &b.key, 1);
			draw_button(p, KEY_SIZE, KEY_SIZE, 1, WHITE, BLACK, WHITE, c, MEDIUM);
		}
	}
	else if(is_arrow_butt(b)){
		int dir = get_arrow_dir(b);
		draw_arrow(p, ARROW_SIZE, ARROW_SIZE, 1, WHITE, BLACK, dir, 1);
	}
	else if(is_screen_butt(b)){
		char* text = get_butt_text(b);
		char* textArray[] = {text, ""};
		draw_menu(p, SBUTT_WIDTH, SBUTT_HEIGHT, 1, WHITE, BLACK, WHITE, SMALL, textArray);
	}
}

void unhighlight(Button b){
	Point p = {b.left, b.top};
	if(is_kb_butt(b)){
		if(is_big_kb(b)){
			draw_button(p, KEY_SIZE*2, KEY_SIZE, 1, BLACK, WHITE, BLACK, b.text, MEDIUM);
		}
		else{
			char* c = "A";
			strncpy(c, &b.key, 1);
			draw_button(p, KEY_SIZE, KEY_SIZE, 1, BLACK, WHITE, BLACK, c, MEDIUM);
		}
	}
	else if(is_arrow_butt(b)){
		int dir = get_arrow_dir(b);
		draw_arrow(p, ARROW_SIZE, ARROW_SIZE, 1, BLACK, WHITE, dir, 0);
	}
	else if(is_screen_butt(b)){
		char* text = get_butt_text(b);
		char* textArray[] = {text, ""};
		draw_menu(p, SBUTT_WIDTH, SBUTT_HEIGHT, 1, BLACK, WHITE, BLACK, SMALL, textArray);
	}
}
