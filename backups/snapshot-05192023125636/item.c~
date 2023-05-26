#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include <item.h>
#include <string_utils.h>
#include <net_utils.h>
#include <regex_utils.h>

#include <curl/curl.h>
#include <libs/nxjson.h>

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
		strcat(message, "\n");
	}

	if(item -> type == phone) {
		strcat(message, "❗ No warranty, mga frenny! ❗\n");
	}

	if(item -> no_choosing) {
		strcat(message, "\n❎ No choosing of design/colors ❎\n");
	}

	if(item -> colors != NULL) {
		strcat(message, "\n🌈 Available colors:\n");
			for(int i = 0; item -> colors[i] != NULL; i++) {
				char* color = item -> colors[i];
				strcat(message, "\t");
				strcat(message, color);
				strcat(message, "\n");
			}
		strcat(message, "\n");
	}

	if(item -> pieces != 0) {
		strcat(message, (char*) item -> pieces);
		if(item -> pieces > 1) {
			strcat(message, " pcs.\n");
		} else {
			strcat(message, " pc.\n");
		}
	}

	if(item -> description != NULL) {
		strcat(message, item -> description);
		strcat(message, "\n");
	}

	if(item -> is_cash) {
		sprintf(message, "%s💵 Cash only: P%i\n", message, (int) item -> cash_amount);
	} else {
		strcat(message, "\n✨ Installment Info ✨\n");
		/* append the size here */
		if(item -> size != NULL) {
			strcat(message, "📐 Size: ");
			for(int i = 0; item -> size[i] != NULL; i++) {
				strcat(message, item -> size[i]);

				/* Checks if the array will not end yet */
				if(item -> size[i + 2] != NULL) {
					strcat(message, " x ");
				} else {
					/* a space for the measurement */
					strcat(message, " ");
				}
			}
			strcat(message, "\n");
		}

		/* TODO: Put comma seperators for the numbers */
		char* formatted = new_string(1024 * 10);
		sprintf(formatted, "⬇️  Down: %i"
                                   "\n🪙 %i/week"
                                   "\n🕒 %i weeks to pay"
				   "\n💵 Cash: %i\n",
				   item -> downpayment, item -> weekly_payment,
				   item -> length, item -> cash_amount);
		strcat(message, formatted);
		free(formatted);
	}

	if(item -> type == default_type || item -> type == household_item || item -> type == speaker || item -> type == phone) {
		strcat(message, "\n✅ - Free delivery(nearby areas)/meet up"
                                "\n✅ - Cash/gcash accepted"
                                "\n✅ - Goodpayer only please. 🤗"
                                "\n✅ - Always refer sa latest post for updated prices\n");
	}

	if((item -> type == household_item || item -> type == speaker) && ! item -> is_cash) {
		strcat(message, "\n✨ Para makapagpundar ng 'di nabibigatan, gawin nating hulugan. ✨\n");
	}

	if((item -> type == bag_high_quality || item -> type == bag_low_quality || item -> type == footwear || item -> type == clothes) && ! item -> is_cash) {
		strcat(message, "\n✨ Mukang mayaman kahit nag hulugan 😉 ✨\n");
	}

	return message;
}

/* ids[0] = page's id, ids[1] = post's id */
char** get_page_ids_from_post(char* url) {
	char** ids = new_string_arr(2);
	ids[0] = get_regex_substring(url, "[&]id=([0=9]+)[&]", 1, 2);
	ids[1] = get_regex_substring(url, "[?&]story_fbid=([a-zA-Z0-9_-]+)[&]", 1, 2);

	return ids;
}

/*----
* @url, must be m[obile].facebook.com link.
* @name initialize before passing, the resulting string  will be
* stored in the pointer, so better allocated enough memory to it,
* also free it afterwards.
* @attachments, the array where the image links will be stored.
* initialize beforehand, free() afterwards.
* @access_token, the access_token from Graph API. */
int get_name_attachments_from_post(char* url, char* name, char** attachments, char* access_token) {
	CURL* handle = curl_easy_init();

	char** ids = get_page_ids_from_post(url);
	char graph_url = new_string(1024 * 5);
	sprintf(url, "https://graph.facebook.com/%i_%i?fields=message,attachments&access_token=%i", ids[0], ids[1], access_token);

	char* response = new_string(1024 * 5);
	get_response_from_url(handle, graph_url, response, NULL);
	free(graph_url);

	nxjson* response_json = nx_json_parse(response, 0);
	name = nx_json_get(response_json, "message") -> text_value;

	/* one-liner: attachments -> data -> subattachments -> data */
	nxjson* attachments_arr = nx_json_get(nx_json_get(nx_json_get(nx_json_get(response_json, "attachments"), "data"), "subattachments"), "data");

	for(int i = 0; i < attachments_arr -> children -> length; i++) {
		char* src = nx_json_get(nx_json_item(attachments_arr, i), "src") -> text_value;
		attachments[i] = src;
		printf("%s\n", src);
	}

	nx_json_free(response_json);
}

int item_main(char** args, int argc) {
	char* colors[5] = {"Red", "Green", "Purple", "Violet", NULL};
	char* sizes[10] = {"1", "1", "2", "inches", NULL, NULL};

	struct item* i = initialize_item();

	i -> supplier_price = 1025;
	i -> name = "Food";
	i -> description = "Some description\nThis item contains stuff.\nMany stuff.";
	i -> colors = colors;
	i -> size = sizes;
	i -> type = bag_high_quality;
	i -> no_choosing = 1;

	compute_item_data(i);
	printf(make_post_content(i));

	char* t_url = "https://m.facebook.com/story.php?story_fbid=pfbid0jmCuS69Z7yys79a8DbuBzBtdVpszcADjX6NXCZtv1f56PPwg5c5jdzGLqUNu5dSyl&id=100057227688584&eav=AfazbtZSQu82FzsTiQc-fLuXTxTRcdDXl1ihqHZJSH6XXepV5KBwL2AJLBkH_i1ArX8&m_entstream_source=message_thread&refid=12&__tn__=%2As%2As&paipv=0";
	char* t_access_token = "EAAIxzDzLepEBAIpfKX9E5iHk0PAMFiHd9mbo0tn1ZAfUV8VLADNMoNH3oMy1LTLrcsW7vvss3ZBPAuKrVGmpCpODpJ1R8XpOqfZBghB1r0puK1dn1hTgeZBDGhMnHixFQ6crAAZCU4qyPGLZA4Y13qU8lgliO6IJZAUlNHIe7Nav6HMNbe2xEjZC";
	char* test = new_string(1024 * 5);
	char* test_attachments(12);
	get_name_attachments_from_post(t_url, test, test_attachments, t_access_token);
	free(i);
}
