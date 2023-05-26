#include <stdlib.h>
#include <string.h>

#include <curl/curl.h>

struct recieved_data {
	char* buffer;
};

size_t write_data(void* buffer, size_t size, size_t nmemb, void* userp) {
	struct recieved_data* data = (struct recieved_data*) userp;
	strcat(data -> buffer, (char*) buffer);

	return nmemb;
}

int get_response_from_url(CURL* handle, char* url, char* buffer, char* request) {
	struct recieved_data data = {.buffer = (char*) malloc (sizeof(char) * 1024 * 25)};
	curl_easy_setopt(handle, CURLOPT_URL, url);
	curl_easy_setopt(handle, CURLOPT_WRITEFUNCTION, write_data);
	curl_easy_setopt(handle, CURLOPT_WRITEDATA, &data);

	if(request) curl_easy_setopt(handle, CURLOPT_CUSTOMREQUEST, request);

	int success = curl_easy_perform(handle);

	strcat(buffer, data.buffer);
	// printf("%s\n----\n", buffer);

	return success;
}
