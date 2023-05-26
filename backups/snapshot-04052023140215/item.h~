#include <item_types.h>

struct item {
	char* name;
	char* description;
	char* description_story;
	char** size;
	char** colors;
	int pieces;

	enum item_type type;

	/* booleans */
	int is_cash;
	int allow_story_caption_same;
	int allow_size_in_story;
	int allow_color_in_story;
	int allow_pieces_in_story;
	int no_choosing;

	/* data */
	int supplier_price;
	int downpayment;
	int weekly_payment;
	int length;
	int cash_amount;

	char** attachments;
};



void compute_item_data(struct item* item);
char* make_post_content(struct *item item);
int item_main(char** args, int argc);
