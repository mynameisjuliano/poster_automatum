#include <stdlib.h>

char* new_string(int size) {
	char* ptr = (char*) malloc(sizeof(char) * size);
	ptr[0] = '\0';

	return ptr;
}

char** new_string_arr(int size) {
	char** arr = (char**) malloc(sizeof(char*) * size);
	for(int i = 0; i < size; i++) {
		arr[i] = NULL;
	}

	return arr;
}

/* Must be initialized by new_string_arr */
int get_string_arr_length(char** str_arr) {
	int count = 0;
	while(str_arr[count++] != NULL) {};

	return count;
}

char** split_str(char* str, char* delim, char** ptr_out) {
	int counter = 0;
	char* ptr;
	ptr = strtok(str, delim);
	while(ptr != NULL) {
		ptr_out[counter++] = ptr;
		ptr = strtok(NULL, delim);
	}

	return ptr_out;
}
