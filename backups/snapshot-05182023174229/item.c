#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

struct PostInfo { // change to a more descriptive name;

};

struct item {
	char* name;
	char* description;
	char** size; // check if this is the correct data type
	char* type;
	int is_cash;
	int is_my_day_caption_same;

	int supplier_price;
	int downpayment;
	int weekly_payment;
	int length;
	int cash;


	struct PostInfo* data;
	int images[][10];
};

void computeItemData(struct item* item) {
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

	item -> cash = estimated_cash;
}

char* makePostContent(struct item* item) {
	char message[1024 * 10] = "";

	if(item -> type == "bag_high_quality") {
		strcat(message, "");
	}

	strcat(message, item -> name);

	if(item -> type == "phone") {
		strcat(message, "No warranty, mga frenny!!");
	}

	strccat(message, "\n\n");

	if(strlen(i -> description) > 0) {
		strcat(message, item -> description + "\n");
	}

	if(item -> is_cash) {
		sprintf(message, "%s💵 Cash only: P%i", message, (int) item -> cash);
	} else {
		sprintf(message, "%s"
				"\n⬇️ Down: %,i\n"
				"\n🪙 %,i/week\n"
				"\n🕒 $i"
				"\n"
				, message)
	}
}

int item_main(char** args, int argc) {
	struct item i = {.supplier_price = 1000, .name = "Food"};

	computeItemData(&i);
	printf("i.downpayment: %i\ni.weekly: %i\ni.length: %i\ni.cash: %i.\n", i.downpayment, i.weekly_payment, i.length, i.cash);
	char cat[100] = "cat: ";
	printf(strcat(cat, "this is a test"));
}
