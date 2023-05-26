import com.google.gson.Gson
import internals.Constants
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class WebUtils {
    companion object {
        class IdCatcher {
            var id : String? = null
        }

        /** This function returns an array that contains the post's and page's ID to be access
         * through the Graph API. The value contained in index 0 is the post's ID, in 1, the page's
         * ID.
         */
        fun getFacebookPostPageId(url: String, accessToken: String): Array<String?> {
            val ids = arrayOfNulls<String?>(2)

            for (query: String in URL(url).query.split("&")) {
                val pairValue = query.split("=")
                if (pairValue[0] == "story_fbid")
                    ids[Constants.FACEBOOK_STORY_ID] = pairValue[1]
                if (pairValue[0] == "id")
                    ids[Constants.FACEBOOK_PAGE_ID] = pairValue[1]
            }

            return ids
        }

        /* postId is generated page-id_post-id */
        fun getFacebookPostContent(postId : Array<String?>, accessToken : String) : String {
            return getStringFromHttp("https://graph.facebook.com/${postId[Constants.FACEBOOK_PAGE_ID]}_${postId[Constants.FACEBOOK_STORY_ID]}?" +
                    "access_token=${accessToken}&fields=id,message,attachments", "GET")
        }

        fun getStringFromHttp(url : String, requestMethod : String) : String {
            val httpConnection = URL(url).openConnection() as HttpURLConnection
            httpConnection.requestMethod = requestMethod

            val scanner = Scanner(httpConnection.inputStream).useDelimiter("//A")
            var content = ""
            while(scanner.hasNext()) content += scanner.next()
            return content;
        }

        /* postId is generated page-id_post-id */
        fun deletePost(url : String, accessToken: String) {
            val ids = getFacebookPostPageId(url, accessToken)

            val response = getStringFromHttp(
                "https://graph.facebook.com/${ids[Constants.FACEBOOK_PAGE_ID]}_${ids[Constants.FACEBOOK_STORY_ID]}?fields=id&access_token=${accessToken}",
                "GET"
            )

            val id = Gson().fromJson(response, IdCatcher::class.java).id

            WebUtils.getStringFromHttp("https://graph.facebook.com/${id}?" +
                    "access_token=${accessToken}", "DELETE")
        }

        fun saveFileFromHttp(url : String, file : File) {
            val httpConnection = URL(url).openConnection() as HttpURLConnection
            httpConnection.requestMethod = "GET" // I think most file downloads use GET

            if(!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

	    println(url);
            /* Save the byte to a file! */
            file.writeBytes(httpConnection.inputStream.readBytes())
        }

    }
}
