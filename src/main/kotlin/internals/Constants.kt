package internals

import java.io.File

class Constants {
    companion object {
        const val DEBUG = true

        val HOME = File(System.getProperty("user.home"))

	/* TODO: Move the image cache files to the new directory, also
	* update the file pointers in @{link FacebookScraper.kt}*/
        val CACHE_WORKSPACE = "${HOME}/.cache/fb"
        val IMG_CACHE_WORKSPACE = "${HOME}/.cache/fb/image_cache"
        val POSTED_DB = "${HOME}/.local/share/fb/posted"
        val RESULTS_WORKSPACE = "${HOME}/Pictures/My Day Results/"
        val POST_LIST = "${CACHE_WORKSPACE}/auto_poster/post_list.data"

        const val NO_ID = "N/A"

        const val FACEBOOK_PAGE_ID = 0
        const val FACEBOOK_STORY_ID = 1

    }
}
