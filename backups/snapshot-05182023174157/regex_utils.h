#include <regex.h>

char* get_regex_substring_offset(char* string, char* pattern, int offset, int group, int maxgroups) {
	char* ptr = "";

	regex_t regex;
	regmatch_t match[maxgroups];
	int return_val = regcomp(&regex, pattern, REG_EXTENDED | REG_ICASE);

	if(return_val) {
		fprintf(stderr, "Can't compile regex. Pattern: {%s}\n", pattern);
		return NULL;
	}

	return_val = regexec(&regex, string, maxgroups, match, REG_EXTENDED | REG_ICASE);

	if(!return_val) {
		int size = match[group].rm_eo - match[group].rm_so;
		int offset_size = size - offset;
		if(size < offset) {
			fprintf(stderr, "Offset is bigger than the regex's ending offset. offset: %i, match.rm_eo.%i\n", match[group].rm_so + offset, match[group].rm_eo);
			exit(1);
		}
		ptr = (char*) malloc(sizeof(char*) * offset_size);
		strncpy(ptr, string + match[group].rm_so + offset, offset_size * sizeof(char));
		ptr[size] = '\0';
	} else if(return_val == REG_NOMATCH) {
		printf("No regex no match. Pattern: {%s}", pattern);
		return NULL;
	} else {
		regerror(return_val, &regex, string, sizeof(string));
		fprintf(stderr, "Regex match caught an error: %s.\n", string);
		return NULL;
	}

	regfree(&regex);
	return ptr;
}

char* get_regex_substring(char* string, char* pattern, int group, int maxgroups) {
	return get_regex_substring_offset(string, pattern, 0, group, maxgroups);
}
