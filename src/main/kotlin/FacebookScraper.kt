import collage.Collage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import internals.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception
import java.lang.NumberFormatException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Scanner
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.system.exitProcess
import kotlin.collections.arrayListOf
import kotlin.text.Regex

class FacebookScraper {
    companion object {
        /** The object that encapsulates the data used for posting for Facebook.
         * Most of the time, it will just inherit data from PostInfo. Its purpose
         * is to differentiate two functions with different nuances and purposes.
         */

	public class BAGInfo {
		companion object {
			val BAG_TYPE_BACKPACK = "backpack"
			val BAG_TYPES = arrayOf<String>("handbag", "slingbag", "sidebag", "beltbag", "bodybag", "backpack", "shoulderbag")

			/* Let the other functions handle empty output */
			fun getBagName(message : String) : String {
				var curTypes = arrayListOf<String>()

				val emoji = if(Pattern.compile(BAG_TYPE_BACKPACK).matcher(message).find()) "🎒" else "👜"
				val lowerCasedMessage = message.lowercase()
				BAG_TYPES.forEach { bagType ->
					/* because sometimes its referred to as *type*\ bag (with spaces) instead of *type*bag */
					val seperated = if (bagType.contains("bag")) {
						bagType.substring(0, bagType.indexOf("bag")) + " bag"
					} else if (bagType.contains("pack")) {
						bagType.substring(0, bagType.indexOf("pack")) + " pack"
					} else {
						bagType
					}
					if(Pattern.compile("[,\\s^]?(${bagType}|${seperated})[,\\s$]?").matcher(lowerCasedMessage).find()) {
						curTypes.add(bagType)
					}
				}

				// generate names
				var name = ""
				for(i in 0..curTypes.size - 1) {
					if(i == 0) {
						name += "$emoji ${curTypes[i].substring(0, 1).uppercase() + curTypes[i].substring(1)}"
					} else if(i < curTypes.size - 1) {
						name += ", ${curTypes[i].substring(0, 1).lowercase() + curTypes[i].substring(1)}"
					} else {
						name += " & ${curTypes[i].substring(0, 1).lowercase() + curTypes[i].substring(1)} $emoji"
					}
				}

				return name
			}
		}
	}
        public class PostInfoFinal() : PostInfo() {

        }
        /** The class that is used to encapsulate the data parsed from the input
         * files of links. It's here for that only very reason.
          */


        public class PostInfoObject {
		@SerializedName("l")
		var link = ""
		@SerializedName("n")
		var name : String? = null
		@SerializedName("p")
		var price : String? = null
		@SerializedName("s")
		var size : String? = null
		@SerializedName("c")
		var caption : String? = null
		@SerializedName("mdd")
		var myDayCaption : String? = null
		@SerializedName("mdc")
		var captionInMyDay = false
		@SerializedName("pc")
		var pieces : String? = null
		@SerializedName("mdpc")
		var piecesInMyDay = true
		@SerializedName("mds")
		/* Note! disable in the bags! */
		var sizeInMyDay = true
		@SerializedName("nc")
		var noChoosingColors = false
		@SerializedName("ds")
		var designs = arrayListOf<String>()
		@SerializedName("cl")
		var colors = arrayListOf<String>()
		@SerializedName("i")
		var includes = arrayListOf<String>()
		@SerializedName("mdcl")
		var colorsInMyDay = true
		@SerializedName("ddc")
		var designsInMyDay = true
		@SerializedName("mdi")
		var includesInMyDay = true
		@SerializedName("t")
		var type = "--"
		var attachments : ArrayList<String>? = null
		@SerializedName("clms")
		var columns : Int = 0 /* 0 means you let the program handle the columns */
		/* Single image files needs to be adjusted! */
		@SerializedName("ai")
		var adjustImage : Boolean = false
		var split : Boolean ? = null /* Forgot what this variable does */
        }



	/* Old format!
        public class PostInfoObject {
            var link = ""
            var name : String? = null
            var price : String? = null
            var size : String? = null
            var caption : String? = null
            @SerializedName("my_day_caption")
            var myDayCaption : String? = null
	    @SerializedName("size_in_my_day")
	    var sizeInMyDay : Boolean = false
	    var type = Item.Companion.Type.DEFAULT
            var attachments : arrayList<String>? = null
            var columns : Int = 0 /* 0 means you let the program handle the columns */
	    @SerializedName("adjust_image")
	    var adjustImage : Boolean = false
            var split : Boolean ? = null /* Forgot what this variable does */
        } */

        /** gs the name implies, it is used for capturing the JSON data response gathered from
         * asking for a page access token from Facebook Graph gPI .
         */
        public class AccessTokenResponse {
            @SerializedName("access_token")
            var accessToken = ""

        }

	class DataList {
		var date : String? = null
		var list = arrayListOf<PostInfoObject>()
	}

        // TODO: Post the JSON structure of a post!
        /** This one heavy data of a class is used to gather information from
         * a post that is requested from Facebook's Graph gPI. Each member will have
         * an explanation, if necessary.
         * The JSON is structured like this:
         * {"id":[PAGE_ID],
         * "message":[CONTENT],
         * "attachments":[{}]
         */
        class FacebookPostDataResponse {
            /* Post's story_fbid, which is basically the Post's ID, used in conjunction
            * with a page's ID, i.e.: ${PAGE_ID}_${STORY_FBID}
             */
            var id : String? = null
            /* The text content of a post */
            var message : String = ""
            /* The JSON class of data of attachments */
            var attachments : Attachments? = null

            companion object {
                /* The second-highest level of a Facebook post response structure */
                class Attachments {
                    var data = ArrayList<AttachmentData>()
                }

                /* Third-level data structure in a Facebook post response structure */
                class AttachmentData {
                    var media : Media? = null
                    var subattachments : SubAttachments? = null
                }

                /* Multiple-upload photo posts have the key "subattachments" in their objects.
                * This is the class for it
                 */
                class SubAttachments {
                    var data = ArrayList<SubAttachmentData>()
                }

                /* Fourth-level structure that contains the data of a "subattachment" key */
                class SubAttachmentData {
                    var media : Media? = null
                }

                /* Fifth-level structure that contains the majority of data of
                 * a "subattachments[n]" key */
                class Media {
                    var image : Image? = null
                }

                /* Deepest-level class for the information about an attachment (e.g. photo)
                * of a post. It contains some photo metadata and their important link.
                 */
                class Image {
                    var width = 0
                    var height = 0
                    var src : String = ""
                }
            }
        }

        private class IdResponse {
            var id = ""
        }

        /* An encapsulated object of the processed information gathered from a Facebook post's
        * JSON response. It contains the key data to make a post anHigh quality b process it
         */
        open class PostInfo() {
            var id : String = ""
            var message : String = ""
            var attachments = arrayListOf<String>();

            constructor(postInfo : PostInfo) : this() {
                this.id = postInfo.id
                this.message = postInfo.message
                this.attachments = postInfo.attachments
            }

            constructor(id : String, message : String, attachments : ArrayList<String>?) : this() {
                this.id = id
                this.message = message
                if(attachments != null) {
			this.attachments.addAll(attachments)
		}
            }

            fun makeCopy() : PostInfo {
                return PostInfo(this)
            }
        }

        /* Generate an Item class data from the url, the additional parameters except @param accessToken is used for
        * manual data substitution, mostly used whenever is reading data from the input file.
         */
        fun scrapeItemFromUrl(url : String, accessToken : String) : Item {
            /** Parse the story_fbid and (page) id from m.facebook.com url. */
            val ids = WebUtils.getFacebookPostPageId(url)
            /** Parse the json then generate a PostInfo from it */
	    val cacheFile = File(Constants.CACHE_WORKSPACE, "json_response/${ids[0]}_${ids[1]}.json")

	    if(!cacheFile.parentFile.exists()) {
		cacheFile.parentFile.mkdirs()
	    }

	    val content = if(cacheFile.exists()) {
		var jsonContent = ""
            	val scanner = Scanner(FileReader(cacheFile)).useDelimiter("//a")
            	while(scanner.hasNext()) {
                	jsonContent += scanner.next()
            	}
		/* println("Reading from file... $cacheFile") */
		jsonContent
	    } else {
		val jsonContent = WebUtils.getFacebookPostContent(ids, accessToken)
		cacheFile.createNewFile()
		val fwriter = FileWriter(cacheFile)
		fwriter.write(jsonContent)
		fwriter.close()
		/* println("Reading from stream, writing to file: $cacheFile") */
		jsonContent
	    }

            val postInfo : PostInfo = generatePostInfoFromJSON(content)

            /** See the item format **/
            val item = when(ids[Constants.FACEBOOK_PAGE_ID]) {
                Item.Companion.Supplier.ANNA_CAI_BAGS,
		Item.Companion.Supplier.ANNA_CAI_BAGS_I    -> processAnnaCaiBags(postInfo.message)
                Item.Companion.Supplier.BEA_TAN_REYES_BAGS -> processBeaTanReyesBags(postInfo.message)
                Item.Companion.Supplier.DIANNE_SOTTO       -> processDianneSotto(postInfo.message)
		Item.Companion.Supplier.KK_STORE           -> processKkStore(postInfo.message)
		Item.Companion.Supplier.WANG_STORE         -> processWangStore(postInfo.message)
		Item.Companion.Supplier.WANG_BAGS          -> processWangStoreBags(postInfo.message)
		Item.Companion.Supplier.LISA_SY            -> processLisaSy(postInfo.message)
                else -> Item("", 0.0)
            }

	    item.data = postInfo
            return item
        }

        private fun processAnnaCaiBags(message : String) : Item {
		val priceMatcher = Pattern.compile("[Rs][Ss]\\s*([0-9]+)").matcher(message)
		val price = if(priceMatcher.find()) {
			try {
				priceMatcher.group(1).toDouble()
			} catch (nfe : NumberFormatException) {
				println("An 'anna cai bag' item has no normal formatted price. Depending on manual input");
				0.0
			}
		} else {
			println("An 'anna cai bag' item has no normal formatted price. Depending on manual input");
			0.0
		}
                val sizeMatcher = Pattern.compile("[Ss][Ii][Zz][Ee]\\s?+:\\s?+(.*+)").matcher(message)
                val size = if(sizeMatcher.find()) {
			sizeMatcher.group(1).replace('*', 'x')
		} else {
			println("An 'anna cai bag' item has no normal formatted size. Skipping")
			null
		}

		var name : String
		val bagName = BAGInfo.getBagName(message)

            	if(bagName != "") {
			name = bagName
		} else {
			val splitMessage = message.split("\n")
			if(splitMessage.size > 1) {
				name = splitMessage[1]
			} else {
				name = "👜 BAG 👜"
			}
		}

		return Item(name, price.toDouble()).setSize(size)
                	.setType(Item.Companion.Type.BAG_LOW_QUALITY)
        }

        fun processBeaTanReyesBags(message : String) : Item {
		/* keep these values in lowercase */
		val priceMatcher = Pattern.compile(".*:\\s*([0-9]+)\\s*ONLY", Pattern.CASE_INSENSITIVE).matcher(message)

                val price = if(priceMatcher.find()) {
			try {
				priceMatcher.group(1).toDouble()
			} catch (nfe : NumberFormatException) {
				println("g 'bea tan reyes bags' item has no normal formatted price. Depending on manual input");
				0.0
			}
		} else {
			println("g 'bea tan reyes bags' item has no parse-able pirce. Depending on manual input.\n$message");
			0.0
		}

                val sizeMatcher = Pattern.compile("SIZE\\s?+:\\s?+(.*+)", Pattern.CASE_INSENSITIVE).matcher(message)
                val size = if(sizeMatcher.find()) {
			sizeMatcher.group(1).replace("inch", "") + " in"
		} else {
			println("a bea tan bag item doesn't have a parsable size info")
			null
		}

		val name = BAGInfo.getBagName(message)
        	return Item(if(name == "") "👜 High quality bag 👜" else name, price).setSize(size)
                	.setType(Item.Companion.Type.BAG_HIGH_QUALITY)
        }

        fun processDianneSotto(message : String) : Item {
		val priceMatcher = Pattern.compile("\\s?+([0-9]+)\\s?+[Oo]?[Nn]?[Ll]?[Yy]?\\s?+", Pattern.CASE_INSENSITIVE).matcher(message)
		priceMatcher.find()
		val price = try {
			priceMatcher.group(1).toDouble()
		} catch ( nfe : NumberFormatException) {
			println("a 'dianne sotto' item has no normal formatted price. Depending on manual input");
			0.0
		}

		return Item("Household Item", price)
                .setType(Item.Companion.Type.HOUSE_ITEM)
        }

	private fun processKkStore(message : String) : Item {
                val priceMatcher = Pattern.compile("ONLY[-\\s\\:]*([0-9]+)", Pattern.CASE_INSENSITIVE).matcher(message)
                val price = if(priceMatcher.find()) {
			priceMatcher.group(1).toDouble()
		} else {
			println("g 'kk_store' item has no normal formatted price. Depending on manual input");
			0.0
		}

		val sizeMatcher = Pattern.compile("[Ss][Ii][Zz][Ee][:]+\\s?+(.+)").matcher(message)
                val size = if(sizeMatcher.find()) {
			sizeMatcher.group(1).replace("[Cc][Mm]", "") + " cm"
		} else {
			null
		}

		return Item("🏠 Cheap household item 💼", price)
			.setSize(size)
			.setType(Item.Companion.Type.CHEAP_HOUSE_ITEM)
	}

	private fun processWangStore(message : String) : Item {
		val priceMatcher = Pattern.compile("P[\\s]?+([0-9,]+)").matcher(message)
		val price = if(priceMatcher.find()){
			priceMatcher.group(1).replace(",", "").toDouble()
		} else {
			println("a 'wang_store' item has no normal formatted price. Depending on manual input");
			0.0
		}

		return Item("🏘️ Household item 💼", price)
			.setType(Item.Companion.Type.HOUSE_ITEM)
	}

	private fun processWangStoreBags(message : String) : Item {
                val priceMatcher = Pattern.compile("P[\\s]?+([0-9,]+)").matcher(message)
                val price = if(priceMatcher.find()){
			priceMatcher.group(1).replace(",", "").toDouble()
		} else {
			println("a 'wang_bag' item has no normal formatted price. Depending on manual input");
			0.0
		}

		val name = BAGInfo.getBagName(message)

		return Item(if(name != "") name else "👜 BAG 👜", price)
			.setType(Item.Companion.Type.BAG_HIGH_QUALITY)
	}

	private fun processLisaSy(message : String) : Item {
                val priceMatcher = Pattern.compile(":[\\s]?+([0-9,]+)p💵").matcher(message)
                val price = if(priceMatcher.find()){
			priceMatcher.group(1).replace(",", "").toDouble()
		} else {
			println("a 'lisa_sy' item has no normal formatted price. Depending on manual input");
			0.0
		}

		val split = message.split("\n")
		val name = split[2].replace("✅", "")
		var caption = ""
		for(i in 3..split.size - 2) {
			val desc = split[i].replace("✅", "")
			caption += "👉 ${desc}"

			if(i != split.size - 2) {
				caption += "\n"
			}
		}

		val sizeMatcher = Pattern.compile("Size\\s?+:\\s?+(.+)[\"“]", Pattern.CASE_INSENSITIVE).matcher(message)
                val size = if(sizeMatcher.find()) {
			sizeMatcher.group(1).replace("x", " x ") + " inches"
		} else {
			println("a 'lisa_sy' item has no parsable size")
			null
		}

		return Item("🔊 Speaker (model: $name) 🔊", price)
			.setSize(size)
			.setCaption(caption)
			.setCaptionInMyDay(caption != "\n" || caption != "")
			.setType(Item.Companion.Type.SPEAKER)
	}


            /** Just generates some raw data from the JSON response from the Graph gPI for
         * the method scrapeItemFromUrl() to use
         **/
        fun generatePostInfoFromJSON(json : String) : PostInfo {
            val postInfo = PostInfo()
            val jsonResponse = Gson().fromJson(json, FacebookPostDataResponse::class.java)

            postInfo.message = jsonResponse.message
            postInfo.id = jsonResponse.id!!

            if(jsonResponse.attachments?.data?.get(0)?.subattachments != null) {
                jsonResponse.attachments?.data?.get(0)?.subattachments?.data?.forEach { it ->
                    postInfo.attachments.add(it.media?.image?.src!!)
                }
            } else {
                jsonResponse.attachments?.data?.forEach { it -> postInfo.attachments.add(it.media?.image?.src!!) }
            }

            return postInfo
        }

        fun makeFacebookPost(attachments : ArrayList<String>, postContent : String,
		pageId : String, accessToken : String) : String {

            val photos = arrayListOf<IdResponse>()
            var attachmentData = ""
            var count = 0

            attachments.forEach { photo ->
                /* Let us post the photos first before gathering it in */

	        /* If the file is local, upload the photo somewhere first
	        * Looks like Facebook allows direct upload using some sort of MIME trickery.
	        * For now I can't really do this, as it will require some studying, but as for
	        * reference, I will be putting the links here.
	        * Facebook gPI documentation ref: https://developers.facebook.com/docs/graph-api/reference/page/photos/
	        * w3 forms reference: https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
	        */

                val response = WebUtils.getStringFromHttp(
                    "https://graph.facebook.com/"
                            + "${pageId}/photos?"
                            + "url=${photo.replace("&", "%26")}&" // Since the url could not be accessed without the queries :)
                            + "published=false&"
                            + "access_token=${accessToken}", "POST"
                )

                val photoResponse = Gson().fromJson(response, IdResponse::class.java)

                photos.add(photoResponse)

                if (count > 0) {
			attachmentData += "&" // a seperator
		}

                attachmentData += "attached_media[${count}]={\"media_fbid\":${photoResponse.id}}"

		count++
            }

            /* Now let us make the post! */
	    val postResponse = WebUtils.getStringFromHttp(
                "https://graph.facebook.com/"
                	+ "${pageId}/feed?"
                	+ "message=${URLEncoder.encode(postContent, "UTF-8")}&"
                	+ "${attachmentData}&"
                	+ "access_token=${accessToken}", "POST")

            return Gson().fromJson(postResponse, IdResponse::class.java).id
        }

	fun createMyDayStory(date : String, item : Item, adjustImage : Boolean, attachments : List<String>, font : String, columns : Int) {
		println("###: Making 'My Day' image for '${item.name}'")
		/* Let us first download the files */
                attachments.forEach { url ->
                    val name = url.split("?")[0].reversed().split('/')[0].reversed()

		    /** 'file://' protocol means local, so there you go! **/
                    val srcImg = if(url.startsWith("file://")) {
			val path = url.substring("file://".length).replace(Regex("^\\~"), Constants.HOME.toString())
			File(path)
		    } else {
			val srcFile = File("${Constants.IMG_CACHE_WORKSPACE}/${item.data!!.id}/${name}")

			/** The latter conditional just checks if the file isn't empty **/
		    	if(srcFile.exists() && srcFile.length() < 1024) {
				srcFile.delete()
		    	}

                    	if (!srcFile.exists()) {
				try {
                       			WebUtils.saveFileFromHttp(url, srcFile)
				} catch (e : Exception) {
					println("Can't access from online or in cache directory: $name")
					// e.printStackTrace();
				}
			}

			srcFile
		    }

		    val image = ImageIO.read(srcImg)
                    if(image != null) {
		    	item.images.add(image)
		    } else {
			println("Path '${url}' can't be accessed, skipping image...")
		    }
                }

		val itemId = if (item.data!!.id == "" || item.data!!.id == Constants.NO_ID) {
		    abs(item.getMessageContent().hashCode())
		} else {
		   item.data!!.id
		}

		if(item.images.size > 0) {
			val stampedDate = date + SimpleDateFormat("-HH-mm-ss").format(Date())
		        val collage = Collage.createStoryFromItem(item, adjustImage, font, columns)
			val files = arrayOfNulls<File>(2)

			when(item.type) {
				Item.Companion.Type.DEFAULT, Item.Companion.Type.CHEAP_HOUSE_ITEM,
				Item.Companion.Type.HOUSE_ITEM -> {
					val prepend = if(item.isCashOnly) {
						"cash"
					} else {
						"${item.length}_weeks"
					}

					files[0] = File("${Constants.RESULTS_WORKSPACE}/$date/${prepend}/"
			    			+ "${prepend}-${item.type}-${stampedDate}-${itemId}.jpeg")
				}
				else -> {
					val prepend = if(item.isCashOnly) {
						"cash"
					} else {
						"${item.length}_weeks"
					}

					files[0] = File("${Constants.RESULTS_WORKSPACE}/$date/${item.type}/"
						+ "${prepend}-${item.type}-${stampedDate}-${itemId}.jpeg")
					if(item.isCashOnly) {
						files[1] = File("${Constants.RESULTS_WORKSPACE}/$date/cash/${prepend}-"
						+ "${item.type}-${stampedDate}-${itemId}.jpeg")
					}
				}
			}

			files.forEach{ file ->
				if(file != null) {
		        		if (!file.exists()) {
						file.parentFile.mkdirs()
		            			file.createNewFile()
					}

		        		ImageIO.write(collage, "jpeg", file)
				}
			}

			item.images.forEach { image ->
			    image.flush()
			}

		        collage.flush()
		}
	}

        fun getExtendedPageAccessToken(appId : String, appSecret : String, userAccessToken : String) : String {
            return WebUtils.getStringFromHttp("https://graph.facebook.com/oauth/access_token?" +
                "grant_type=fb_exchange_token&" +
                "client_id=${appId}&" +
		"client_secret=${appSecret}&" +
                "fb_exchange_token=${userAccessToken}", "GET")
        }

        fun getPageAccessToken(pageId : String, userAccessToken : String) : String {
            return Gson().fromJson(WebUtils.getStringFromHttp("https://graph.facebook.com/${pageId}?" +
                    "fields=access_token&" +
                    "access_token=${userAccessToken}", "GET"),
                AccessTokenResponse::class.java).accessToken
        }

	fun parseInputFilesToUpload(inputFile : File, pageId : String, disablePost : Boolean, disableImageGen : Boolean, font : String,
		offline : Boolean, debug : Boolean, accessToken : String) {
	     println("Processing input file: '${inputFile.absolutePath}'")


	    if(offline) {
		println("WARNING: You are trying to post while the offline flag (-o) is on. Only the manual and cached posts will be debugged.")
	    }

            var jsonContent = ""
            val scanner = Scanner(FileReader(inputFile)).useDelimiter("//a")
            while(scanner.hasNext()) {
                jsonContent += scanner.next()
            }

	    parseJsonToUpload(jsonContent, pageId, disablePost, disableImageGen, font, offline, debug,  accessToken)
	}

        /* This is the method that parse an input file that contains the JSON of supplier's items.
        * @param file, is the file from which to be parsed
        * @param pageId is the pageId of the page to be posted on, (requires a valid page access token)
        * @param isMock, if the post should not be posted but only simulated. (Will still generate images though)
        * @param accessToken is the access token that will be used in the posting.
        * */
        fun parseJsonToUpload (json : String, pageId : String, disablePost : Boolean, disableImageGen : Boolean, font : String, offline : Boolean,
		debug : Boolean, accessToken : String) {
	    val posts = Gson().fromJson(json, DataList::class.java) ?: return

	    val date = posts.date ?: SimpleDateFormat("MM-dd-YYYY").format(Date())
            /* To save the ID for deletion */
            val postedItemsFile = File(Constants.POSTED_DB, "${date}.csv")
	    val postedItemsMap = hashMapOf<Int, String>()
	    if(!postedItemsFile.exists()) {
		postedItemsFile.parentFile.mkdirs()
		postedItemsFile.createNewFile()
	    } else {
		val scanner = Scanner(FileInputStream(postedItemsFile)).useDelimiter("\\a")
                scanner.forEach {
			val value = it.split(", ")
			postedItemsMap.put(value[0].toInt(), value[1])
		}
	    }

	    val postedItemsFwriter = FileWriter(postedItemsFile)

	    var invalidCount = 0
	    var skippedPosts = 0

            posts.list.forEach {
		var invalid = false

                try {
                    var item = Item()
		    var manual = it.link == ""

		    if (!manual) {
			try {
                       		item = scrapeItemFromUrl(it.link, accessToken)
			    	if(offline) {
					println("WARNING: You are trying to post while the offline flag (-o) is on. Make sure you've " +
					"cached the post before doing anything else.")
				}
			} catch (ioe : Exception) {
				ioe.printStackTrace()
				println("Link '${it.link}' can not be reached. Turning to manual posting.")
				if(!invalid) {
					invalid = true
					invalidCount++
				}
			}

                    }

		    val priceConverted = try {
			if(it.price != null || it.price != "") {
				it.price?.toDouble();
			} else {
				throw NumberFormatException("Double is null")
			}
		    } catch (nfe : NumberFormatException) {
			null
		    }

		    item.name = it.name?: item.name
		    item.size = it.size?: item.size
		    item.supplierPrice = priceConverted?: item.supplierPrice
		    item.caption = it.caption?: item.caption
		    item.myDayCaption = it.myDayCaption?: item.myDayCaption
		    item.sizeInMyDay = if(!it.sizeInMyDay) {
			    item.sizeInMyDay
		    } else {
			    it.sizeInMyDay
		    }

		    item.captionInMyDay = if(!it.captionInMyDay) {
			    item.captionInMyDay
		    } else {
			    it.captionInMyDay
		    }

		    item.pieces = it.pieces ?: item.pieces
		    item.piecesInMyDay = if(it.piecesInMyDay) {
			    item.piecesInMyDay
		    } else {
			    it.piecesInMyDay
		    }
		    item.colors.addAll(it.colors)
		    item.colorsInMyDay = it.colorsInMyDay
		    item.designs.addAll(it.designs)
		    item.designsInMyDay = it.designsInMyDay
		    item.includes.addAll(it.includes)
		    item.includesInMyDay = it.includesInMyDay

		    item.type = if(it.type != item.type && it.type != "--") {
			    it.type
		    } else {
			    item.type
		    }

		    item.data = item.data?: PostInfo("", "", it.attachments)
		    item.updateFromSupplierPrice()

		    if(item.supplierPrice <= 0) {
			if(!invalid) {
					invalid = true
					invalidCount++
			}
			println("Item has an invalid/or unusual price:" + item.getMessageContent()
				+ "\ntype: " + item.type + ", name: " + item.type)
		    }

		    if(offline && manual) {
			println("Manual post's message content: ${item.getMessageContent()}")
		    }

		    if(debug) {
			println(item.getMessageContent())
		    }

		    if(!disablePost && !offline) {
			try {
				val itemHash = item.hashCode()
				if(!postedItemsMap.contains(itemHash)) {
			        	println("Posting Post[POST_ID]: \"${item.name}\"")
			        	val id = makeFacebookPost(item.data?.attachments!!, item.getMessageContent(),
						pageId, accessToken)
			        	println("Item posted successfully. POST_ID: \"${id}\"")
					postedItemsFwriter.append("${itemHash}, ${id}\n")
				} else {
					skippedPosts++
					println("Item '${itemHash}' already posted, please delete in the file ${postedItemsFile}. Skipping")
				}
			} catch (ioe : Exception) {
			    ioe.printStackTrace()
			    println("${item.hashCode()} failed to be posted.... Skipping.")
			    if(!invalid) {
			    	invalid = true
			    	invalidCount++
			    }
			}
		    }
		    if(!disableImageGen && !offline) {
			try {
				createMyDayStory(date, item, it.adjustImage, item.data?.attachments!!, font, it.columns)
			} catch (ioe : Exception) {
				ioe.printStackTrace()
				println("Failed to generate my day for ${item.hashCode()}... Skipping.")
				if(!invalid) {
					invalid = true
					invalidCount++
				}
			}
		    }
                } catch ( e : Exception ) {
			e.printStackTrace()
			if(!invalid) {
			    	invalid = true
			    	invalidCount++
			}
                }
            }

	    if(invalidCount < 1) {
		    println("Json successfully parsed.")
	    } else {
		    println("Json parsing encountered errors. Error count: ${invalidCount}")
	    }
	    if(skippedPosts > 0) {
		println("Some items were already posted. Count of skipped posts: ${skippedPosts}")
	    }
	    /* Find ways to close this when a SIGTERM signal is recieved */
	    postedItemsFwriter.close()
        }

        fun deleteAllAutomaticPost(pageId : String, accessToken : String) {
            val file = File(Constants.HOME, Constants.POST_LIST)
            try {
                val scanner = Scanner(FileInputStream(file)).useDelimiter("\\a")
                scanner.forEach {
                    val url = "https://graph.facebook.com/${pageId}_${it}?access_token=${accessToken}"
                    println("Deleting: '$url'")
                    WebUtils.getStringFromHttp(url, "DELETE")
                }

                // Let's remove the contents afterwards and then refresh it.
                file.delete()
                file.createNewFile()
            } catch (e : FileNotFoundException) {
                // Welp, there's not much to do when the file is not found, let's just create it then.
                file.parentFile.mkdirs()
                file.createNewFile()
            }
        }

        fun deleteFacebookPost(url : String, accessToken: String) : String {
		return WebUtils.deletePost(url, accessToken)
        }

        fun sharegPost(postURL : String, groupId : String, message : String, accessToken : String) : String {
            return WebUtils.getStringFromHttp("https://graph.facebook.com/${groupId}/feed?" +
                    "link=${postURL}" +
                    "&message=${URLEncoder.encode(message, "UTF-8")}" +
                    "&access_token=${accessToken}", "POST")
        }

    }

}
