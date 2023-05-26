#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include <item.h>
#include <item_types.h>
#include <string_utils.h>

struct item* initialize_item() {
	struct item* i = (struct item*) malloc(sizeof(struct item));

	i -> name = NULL;
	i -> description = NULL;
	i -> description_story = NULL;
	i -> size = NULL;
	i -> colors = NULL;

	i -> type = default_type;

	i -> is_cash = 0;
	i -> allow_story_caption_same = 0;
	i -> allow_size_in_story = 0;
	i -> allow_color_in_story = 0;
	i -> allow_pieces_in_story = 0;
	i -> no_choosing = 0;

	i -> supplier_price = 0;
	i -> downpayment = 0;
	i -> weekly_payment = 0;
	i -> length = 0;
	i -> cash_amount = 0;

	i -> attachments = NULL;

	return i;
}

void compute_item_data(struct item* item) {
	int installment_price = ceil(item -> supplier_price * 1.5);
	double downpayment_rate = 0.25;
	int estimated_weekly = 0;
	int estimated_down = 0;
	int estimated_cash = 0;

	if(item -> supplier_price < 1000) {
		downpayment_rate = 0.30;
	}

	if(item -> supplier_price < 1000) {
		estimated_down = ceil(installment_price * downpayment_rate);
	} else {
		estimated_down = ceil(item -> supplier_price * downpayment_rate);
	}

	/* Items that are lower than 170 pesos is sold by only cash-to-cash basis */
	if(item -> supplier_price <= 170) {
		item -> is_cash = 1;
	} else {
		item -> is_cash = 0;

		/* if estimated_down is divisble by 5 */
		if(estimated_down % 5 != 0) {
			/* practically round up to 5 the estimated_down */
			estimated_down = estimated_down + 5 - estimated_down % 5;
		}

		/* the time the item has to be paid is dependent */
		if(item -> supplier_price < 1000) {
			item -> length = 4;
		} else if(item -> supplier_price < 2000) {
			item -> length = 6;
		} else if(item -> supplier_price < 4000) {
			item -> length = 8;
		} else if(item -> supplier_price < 7000) {
			item -> length = 10;
		} else if(item -> supplier_price < 9000) {
			item -> length = 12;
		} else if(item -> supplier_price < 14000) {
			item -> length = 16;
		} else {
			item -> length = 20;
		}

		estimated_weekly = (installment_price - estimated_down) / item -> length;

		/* do the same with estimated_down, check if not evened up to be divisible by 5, then round up by 5  */
		if(estimated_weekly % 5 != 0) {
			/* practically round up to 5 the estimated_down */
			estimated_weekly = estimated_weekly + 5 - estimated_weekly % 5;
		}

		item -> downpayment = estimated_down;
		item -> weekly_payment = estimated_weekly;
	}

	estimated_cash = item -> supplier_price * 1.3;

	if(estimated_cash% 5 != 0) {
		/* practically round up to 5 the estimated_down */
		estimated_cash = estimated_cash + 5 - estimated_cash % 5;
	}

	item -> cash_amount = estimated_cash;
}

char* make_post_content(struct item* item) {
	char* message = new_string(1024 * 25);

	if(item -> type == bag_high_quality) {
		strcat(message, "💋 Installment top grade bag 💋\n");
	}

	if(item -> name != NULL) {
		strcat(message, item -> name);
	}

	if(item -> type == phone) {
		strcat(message, "❗ No warranty, mga frenny! ❗");
	}

	if(item -> no_choosing) {
		strcat(message, "❎ No choosing of design/colors ❎");
		strcat(message, "\n");
	}

	if(item -> colors != NULL) {
		strcat(message, "🌈 Available colors: ");
			for(int i = 0; item -> colors[i] != NULL; i++) {
				char* color = item -> colors[i];
				strcat(message, "\t");
				strcat(message, color);
				strcat(message, "\n");
			}
	}

	if(item -> pieces != NULL) {
		strcat(message, (char*) item -> pieces);
		if(item -> pieces > 1) {
			strcat(message, " pcs");
		} else {
			strcat(message, " pc");
		}
		strcat(message, "\n");
	}

	if(item -> description != NULL) {
		strcat(message, item -> description);
		strcat(message, "\n");
	}

	if(item -> is_cash) {
		sprintf(message, "%s💵 Cash only: P%i", message, (int) item -> cash_amount);
	} else {
		/* append the size here */
		if(item -> size != NULL) {
			strcat(message, "📐 Size: ");
			for(int i = 0; item -> size[i] != NULL; i++) {
				char* size_f = item -> size[i];
				strcat(message, size_f);

				/* Checks if the array will not end yet */
				if(item -> size[i + 2] != NULL) {
					strcat(message, " x ");
				}
			}
		}

		/* TODO: Put comma seperators for the numbers */
		char* formatted = new_string(1024 * 10);
		sprintf(formatted, "\n✨ Installment Info ✨"
				   "\n⬇️  Down: %i"
                                   "\n🪙 %i/week"
                                   "\n🕒 %i weeks to pay"
				   "\n💵 Cash: %i\n\n",
				   item -> downpayment, item -> weekly_payment,
				   item -> length, item -> cash_amount);
		strcat(message, formatted);
		free(formatted);
	}

	return message;
}

int item_main(char** args, int argc) {
	char* colors[4] = {"Red", "Green", "Purple", "Violet"};

	struct item* i = initialize_item();
	i -> supplier_price = 1000;
	i -> name = "Food";
	i -> colors = colors;
	i -> type = phone;

	compute_item_data(&i);
	printf(make_post_content(&i));
}