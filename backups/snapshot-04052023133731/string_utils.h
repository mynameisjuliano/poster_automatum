#include <stdlib.h>
#include <ctype.h>

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

int to_uppercase_init(char* str) {
	int spaced = 1;
	for(int i = 0; str[i] != '\0'; i++) {
		if(spaced == 1)	{
			str[i] = toupper(str[i]);
			spaced = 0;
		} else if(str[i] == ' ') {
			spaced = 1;
		}
	}
}

int lowercase(char* str) {
	for(int i = 0; str[i] != '\0'; i++) {
		str[i] = tolower(str[i]);
	}
}

int uppercase(char* str) {
	for(int i = 0; str[i] != '\0'; i++) {
		str[i] = toupper(str[i]);
	}
}
