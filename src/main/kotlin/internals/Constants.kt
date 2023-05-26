package internals

import java.io.File

class Constants {
    companion object {
        const val DEBUG = true

        val HOME = File(System.getProperty("user.home"))

        const val CACHE_WORKSPACE = ".cache/fb"
        const val RESULTS_WORKSPACE = "Pictures/My Day Results/"
        const val POST_LIST = ".cache/auto_poster/post_list.data"

        const val NO_ID = "N/A"

        const val FACEBOOK_PAGE_ID = 0
        const val FACEBOOK_STORY_ID = 1

    }
}