import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import internals.Cli
import internals.Constants
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.Scanner
import kotlin.system.exitProcess

/* TODO #1: Test out the src links of images in each post
 * TODO #2: Test pricing and such.
 */
class Metadata {
    @SerializedName("access_token")
    var accessToken : String? = null
    @SerializedName("page_id")
    var pageId : String ? = null
    @SerializedName("app_id")
    var appId : String ? = null
    @SerializedName("app_secret")
    var appSecret : String ? = null
    @SerializedName("disable_post")
    var disablePost = false
    @SerializedName("disable_image")
    var disableImageGen = false
}

fun main(args: Array<String>) {
    parseArguments(args)
}

private fun parseArguments(args: Array<String>) {

    var disablePost = false
    var disableImageGen = false

    var inputFile = File(Constants.HOME, "post_list.json")
    var accessToken : String = "null"
    var pageId : String? = null
    var appId : String? = null
    var appSecret : String? = null
    var font = "NotoColorEmoji"
    val verbose: Boolean
    val offline : Boolean

    /* Help parser cuts down this details, work on it! Just add an additional argument
    * that will print this message when the option is supplemented. */
    val JSON_INFO = "The json should contain an array that contains one or more json objects that has information about " +
				"the post. Each post information object inside the main array is formatted as such:\n" +
				"[{\"link\": \"...\", \"name\": \"...\", \"price\": 0,\n" +
				"\"size\": \"...\", \"caption\": \"...\", \"my_day_caption\": \"...\",\n" +
				"\"size_in_my_day\": false, \"force_installment\": false, \"columns\": 0, \n" +
				"\"type\": \"footwear\", \"attachments\": [\"image_link1\", \"image_link2\", . . .]}]\n" +
				"----\n" +
				"Object members explained:\n" +
				"- link+ : the m[obile].facebook.com url that data will be parsed off on, only works with " +
				"page posts, not user's as they require to grant access token to the Graph API application " +
				"to be able to access their posts.\nNOTE: If this data does not exist, the user must manually " +
				"provide the data for the post for it to be complete, (i.e. the entry for name*, price*, size+, " +
				"caption+, my_day_caption+, size_in_my_day+, force_installment+, type+, attachments^). Put the " +
				"missing fields in discretion, not all url will be parsed equally, some has to have other info " +
				"to be submitted for the post to be complete, for example, a post's size or price might not be " +
				"parsed correctly, so it's up to you to provide it yourself.\n" +
				"- name* : the name of the item\n" +
				"- price* : the price of the item. If exceeding 170 pesos, it will be calculated as installment, " +
				"otherwise as cash. The latter behavior can be overridden by @force_installment." + "\nNOTE: probably make a force_cash implementation.\n\n" +
				"- size+ : the size data of the item. This is merely a caption with template message.\n" +
				"- caption+ : the caption of the post, mostly a description.\n" +
				"- my_day_caption+ : the supplimentary information that will appear on the top of the generated " +
				"image's caption, mostly for sizes, device info and such.\n" +
				"- size_in_my_day+: boolean, if the size provided in @size should also appear in my day. Reduces " +
				"redundancies in entries.\n" +
				"- force_installment+ : boolean, if the small amount of cash should still be considered as installment.\n" +
				"- columns: the amount of columns you want the story image to have. putting '0' means that you let the pro" +
				"gram handle the column count, which have some pretty OK defaults. \n" +
				"- type*: mandatory but has a default value. This is the type of item that will be posted and so, the " +
				"post's format will be different according to each type, it is hard-coded. Maybe I will implement " +
				"scripted format for new types, but as for now, this is the available types:\n" +
				"\t> \"${Item.Companion.Type.DEFAULT}\" : the default type, this is the basic type that formats the supplemen" +
				"tary info at the footer of the post.\n" +
				"\t> \"${Item.Companion.Type.CHEAP_HOUSE_ITEM}\"\n" +
				"\t> \"${Item.Companion.Type.HOUSE_ITEM}\"\n" +
				"\t> \"${Item.Companion.Type.BAG_LOW_QUALITY}\"\n" +
				"\t> \"${Item.Companion.Type.BAG_HIGH_QUALITY}\" : adds \"💋 Installment high quality bags\" at header of post.\n" +
				"\t> \"${Item.Companion.Type.SHOES_SLIPPERS}\"\n" +
				"\t> \"${Item.Companion.Type.PHONE}\"\n" +
				"\t> \"${Item.Companion.Type.CLOTHES}\"\n" +
				"- attachments^ : the array that contains the image links of pictures to be uploaded to a post. Must be publicly " +
				"available on the web or the link of an image from a Facebook post. !TODO: Support for local files.\n" +
				"----\n" +
				"footnote:\n" +
				"* mandatory, required for post generation\n" +
				"+ optional\n" +
				"^ optional but most likely should be satisfied"
    val options = Options()
    	.addOption(Cli.S_OFFLINE.toString(),        Cli.L_OFFLINE,        false, "Enable making post information and image generation when offline. Mostly used for testing purposes")
        .addOption(Cli.S_ACCESS_TOKEN.toString(),   Cli.L_ACCESS_TOKEN,   true, "The access token needed to access the page to post. Required argument.")
        .addOption(Cli.S_PAGE_ID.toString(),        Cli.L_PAGE_ID,        true, "The page id of where the posts will be posted on. Required argument")
        .addOption(Cli.S_APP_SECRET.toString(),     Cli.L_APP_SECRET,     true, "The application's secret ID needed to generate page access tokens with long expiry")
        .addOption(Cli.S_INPUT_FILE.toString(),     Cli.L_INPUT_FILE,     true, "The file where the JSON of post's data will be accessed and parsed. Default location is: ${inputFile.absolutePath}.")
        .addOption(Cli.S_METADATA_FILE.toString(),  Cli.L_METADATA_FILE,  true, "The file where the configuration is -- formatted in JSON. The JSON must contain the following values, if used: access_token*, page_id*, app_id, app_secret.\n---\n* mandatory")
        .addOption(Cli.S_DISABLE_POST.toString(),   Cli.L_DISABLE_POST,   false, "Outputs the generated text of posts instead of actually posting them on Facebook. To be clear, this disables Facebook posting.")
        .addOption(Cli.S_DISABLE_IMAGE.toString(),  Cli.L_DISABLE_IMAGE,  false, "Disables image generation for my day")
        .addOption(Cli.S_DELETE_POST.toString(),    Cli.L_DELETE_POST,    true, "The m.facebook.com links that will be deleted. Comma-separated values.")
        .addOption(Cli.S_FONT.toString(),           Cli.L_FONT,           true, "The name of the font installed on the system that will be used for story images. Default is \"${font}\"")
        .addOption(Cli.S_VERBOSE.toString(),        Cli.L_VERBOSE,        false, "Log the error and additional information.")
        .addOption(Cli.S_EXTENDED_TOKEN.toString(), Cli.L_EXTENDED_TOKEN, false, "Prints out an extended token (page or user) to input stream and then terminates the process.")
        .addOption(Cli.S_HELP.toString(),           Cli.L_HELP,           false, "Display this help output.")
	.addOption(Cli.S_DELETE_ALL.toString(),     Cli.L_DELETE_ALL,     false, "Delete all posts that are generated by this app.")
        .addOption(Cli.S_RAW_JSON.toString(),       Cli.L_RAW_JSON,       true, "Directly input a JSON array instead of a file. Supports multiple values.")
	.addOption(Cli.S_JSON_INFO.toString(),      Cli.L_JSON_INFO,      false, "Display the data required for -i data generation.")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    var tempAccessToken = cmd.getOptionValue(Cli.S_ACCESS_TOKEN, null)

    if(cmd.hasOption(Cli.S_HELP)) {
        HelpFormatter().printHelp("poster -TODO PRINT ALL THOSE VARIABLES DUDE!", "\nNOTE: OpenJDK/Oracle does not " +
                "support rendering emojis in text, at least in jdk8, so try a different jre distribution " +
                "if you want that support. JBR (JetBrainsRuntime) jre8 works with emojis, not sure with others, please " +
                "test and report if you have time to spare.", options, "")
        exitProcess(0)
    }

    if(cmd.hasOption(Cli.S_JSON_INFO)) {
	JSON_INFO.split('\n').forEach { line ->
		println(line)
	}
	exitProcess(0)
    }

    offline = cmd.hasOption(Cli.S_OFFLINE)

    /* Parse the metadata JSON file if it exists */
    var metadata : Metadata?

    if(cmd.hasOption(Cli.S_METADATA_FILE)) {
        val dest = cmd.getOptionValue(Cli.S_METADATA_FILE)
        val file = try {
            File(dest)
        } catch (fnf : FileNotFoundException) {
            println("File: '${dest}' not found")
            throw fnf
        }

        var json = ""
        Scanner(FileReader(file)).useDelimiter("//A").forEach {line ->
            json += line
        }

        metadata = Gson().fromJson(json, Metadata::class.java)
        pageId = metadata.pageId
        if(!offline) {
		tempAccessToken = metadata.accessToken!!
		accessToken = FacebookScraper.getPageAccessToken(pageId!!, tempAccessToken)
	}
        appId = metadata.appId
        appSecret = metadata.appSecret
        disablePost = metadata.disablePost
        disableImageGen = metadata.disableImageGen
    }

    if(pageId == null && cmd.hasOption(Cli.S_PAGE_ID) && !offline) {
        pageId = cmd.getOptionValue(Cli.S_PAGE_ID)
    } else if(pageId == null && !offline){
        println("Argument -p is required, or has an invalid value. Please check your arguments.")
        exitProcess(1)
    }

    /* Not sure what's going to happen with this conditional of 'accessToken == "null"',
    * should just I use non-null assertions instead? */
    if(tempAccessToken != null && pageId != null && cmd.hasOption(Cli.S_ACCESS_TOKEN) && !offline) {
        accessToken = FacebookScraper.getPageAccessToken(pageId!!, tempAccessToken)
    } else if(accessToken == "null" && !offline){
        println("Argument -t is required, or has an invalid value. Please check your arguments.")
        exitProcess(1)
    } else if(offline) {
	println("Ignoring access token requisite since running in offline mode '-${Cli.S_OFFLINE}'")
    }

    appId = cmd.getOptionValue(Cli.S_APP_ID, appId)
    appSecret = cmd.getOptionValue(Cli.S_APP_SECRET, appSecret)

    if(cmd.hasOption(Cli.S_EXTENDED_TOKEN) && appId != null && appSecret != null && tempAccessToken != null && !offline) {
        println("Extended access token: ${FacebookScraper.getExtendedPageAccessToken(appId, appSecret, tempAccessToken)}. Please keep this ID.")
	exitProcess(0)
    } else if(cmd.hasOption(Cli.S_EXTENDED_TOKEN) && (appId == null || appSecret == null || tempAccessToken == null) && !offline){ // Yeah, check the other variables instead
        println ("Generating extended access tokens requires arguments -${Cli.S_APP_ID}, " +
                "-${Cli.S_APP_SECRET}, ${Cli.S_ACCESS_TOKEN}, or at least the values " +
                "app_id, app_secret and access_token are supplied in the JSON file found in -${Cli.S_METADATA_FILE}")
		println("appId == null: ${appId == null}, appSecret == null : ${appSecret == null}, "
			+ "tempAccessToken == null : ${tempAccessToken == null}")
		exitProcess(1)
    } else if(cmd.hasOption(Cli.S_EXTENDED_TOKEN) && offline) {
	println("Ignoring argument -${Cli.S_EXTENDED_TOKEN} since running in offline mode.")
    }

    if(cmd.hasOption(Cli.S_DELETE_ALL) && !offline)  {
        println("Deleting all automatically posted stories...")
        FacebookScraper.deleteAllAutomaticPost(pageId!!, accessToken)
        println("Success! Exiting...")
        exitProcess(0)
    }

    disablePost = cmd.getOptionValue(Cli.S_DISABLE_POST, disablePost.toString()).toBoolean()
    disableImageGen = cmd.getOptionValue(Cli.S_DISABLE_IMAGE, disableImageGen.toString()).toBoolean()

    verbose = cmd.hasOption(Cli.S_VERBOSE)

    if(cmd.hasOption(Cli.S_DELETE_POST)) {
        cmd.getOptionValues(Cli.S_DELETE_POST).forEach { link ->


	    val id = WebUtils.getFacebookPostPageId(link, accessToken)[Constants.FACEBOOK_STORY_ID]
            println("Deleting post: '$id'")

            try {
                FacebookScraper.deleteFacebookPost(link, accessToken)
                println("Delete success!")
            } catch (ioe : IOException) {
                println("Deletion failed...")
                if(verbose) {
                    println("Reason: ${ioe.stackTraceToString()}")
                }
            }
        }

        exitProcess(0)
    }

    if(cmd.hasOption(Cli.S_FONT)) {
        font = cmd.getOptionValue(Cli.S_FONT, font)
    }

    if(cmd.hasOption(Cli.S_RAW_JSON)) {
	cmd.getOptionValues(Cli.S_RAW_JSON).forEach { json ->
		/* Let's process the json's */
    		FacebookScraper.parseJsonToUpload(json, pageId!!, disablePost, disableImageGen, font, offline, accessToken)
	}
    }

    if(cmd.hasOption(Cli.S_INPUT_FILE)) {

	cmd.getOptionValues(Cli.S_INPUT_FILE).forEach { path ->
		/* Let's process the file! */
    		FacebookScraper.parseInputFilesToUpload(File(path), pageId!!, disablePost, disableImageGen, font, offline, accessToken)
	}
    } else if(!cmd.hasOption(Cli.S_RAW_JSON)) {
	FacebookScraper.parseInputFilesToUpload(inputFile, pageId!!, disablePost, disableImageGen, font, offline, accessToken)
    }

}

fun testPrices() {
    println(Item("Iron 🤩", 170.0).getMessageContent())
    println(Item("Bag 🤩", 250.0).setType(Item.Companion.Type.BAG_LOW_QUALITY).setSize("28.5 x 7 x 2.5 cm").getMessageContent())
    println(Item("Bag 🤩", 350.0).setType(Item.Companion.Type.BAG_HIGH_QUALITY).setSize("28.5 x 7 x 2.5 in").getMessageContent())
    println(Item("House Item 🤩", 1450.0).setType(Item.Companion.Type.HOUSE_ITEM).getMessageContent())
    println(Item("Phone 🤩", 1450.0).setType(Item.Companion.Type.PHONE).getMessageContent())
}
