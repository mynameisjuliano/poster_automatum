import java.awt.image.BufferedImage
import kotlin.math.ceil

class Item {
    companion object {
        class Type {
            companion object {

                const val DEFAULT = "none"
                const val CHEAP_HOUSE_ITEM = "cheap_house_item";
                const val HOUSE_ITEM = "house_item"
                const val BAG_LOW_QUALITY = "bag_low_quality"
                const val BAG_HIGH_QUALITY = "bag_high_quality"
                const val SHOES_SLIPPERS = "footwear"
                const val PHONE = "phone"
		const val CLOTHES = "clothes"
		const val SPEAKER = "speaker"
            }
        }

	/* array to have the name */
        class Supplier {
		companion object {
			val ANNA_CAI_BAGS =      "100063763044968"// "👜 Anna Cai Bags"]
			val ANNA_CAI_BAGS_I =    "100087072821516"
			val BEA_TAN_REYES_BAGS = "100057227688584"// "👜 Bea Tan Reyes Bags"]
			val DIANNE_SOTTO =       "100064897175497"// "Dianne Sotto"]
			val KK_STORE = 		 "100057183110957"
			val WANG_STORE =         "100064875485080"
			val WANG_BAGS =          "100093492876308"
			val LISA_SY =            "100057411379525"
		}
	}
     }

    /* Meta-data related variables */
    var name : String? = null
    var caption : String? = null
    var captionInMyDay = false
    var myDayCaption : String? = null
    /* Pay attention to this when parsing. TODO: Implement this. */
    var size : String? = null
    var pieces : String? = null
    var piecesInMyDay = true
    var sizeInMyDay = false
    var noDesign = false
    var colors = arrayListOf<String>()
    var designs = arrayListOf<String>()
    var includes = arrayListOf<String>()
    var colorsInMyDay = true
    var designsInMyDay = true
    var includesInMyDay = true
    var type = Type.DEFAULT
    var isCashOnly = false

    /* Price-related variables */
    var supplierPrice = 0.0
    var downPayment = 0.0
    var weeklyPayment = 0.0
    var length = 0
    var cash = 0.0

    /* initialization and cache */
    private var initialized = false
    private var messageContent = ""

    /* Data-related variables */
    var data : FacebookScraper.Companion.PostInfo? = null
    var images = arrayListOf<BufferedImage>()

    constructor() {

    }

    constructor(name : String, supplierPrice : Double) {
        this.name = name
        this.supplierPrice = supplierPrice
        updateFromSupplierPrice()
    }

    /* Setting the value of @variable supplierPrice is necessary before
    * calling this function.
    */
    fun updateFromSupplierPrice() {
        val isLowerThan1k = supplierPrice < 1000
        val installmentPrice = ceil(supplierPrice + supplierPrice * 0.5)
        val downPaymentRate = if (isLowerThan1k) 0.25 else 0.30
        var estimatedWeekly: Double


        var estimatedDown = ceil(
            (if (isLowerThan1k)
                installmentPrice
            else supplierPrice) * downPaymentRate
        )

	if(type == Type.HOUSE_ITEM || type == Type.CHEAP_HOUSE_ITEM) {
		if(supplierPrice < 250) {
			type = Type.CHEAP_HOUSE_ITEM
		} else {
			type = Type.HOUSE_ITEM
		}
	}

        if(supplierPrice <= 170) {
        	isCashOnly = true
        } else {
		isCashOnly = false

		if (!MathUtils.isDivisible(estimatedDown, 5.0)) {
            		estimatedDown = MathUtils.ceilByValue(estimatedDown, 5.0)
        	}

		length = if(isLowerThan1k) {
			4
		} else {
			if(supplierPrice < 2000) 6
			else if(supplierPrice < 4000) 8
			else if(supplierPrice < 7000) 10
			else if(supplierPrice < 9000) 12
			else if(supplierPrice < 14000) 16
			else 20
		}

		estimatedWeekly = (installmentPrice - estimatedDown)/length

		if(!MathUtils.isDivisible(estimatedWeekly, 5.0)) {
		     estimatedWeekly = MathUtils.ceilByValue(estimatedWeekly, 5.0)
		}

		downPayment = estimatedDown
		weeklyPayment = estimatedWeekly
	}

        var estimatedCash = supplierPrice + supplierPrice * 0.3

        if(!MathUtils.isDivisible(estimatedCash, 5.0)) {
          estimatedCash = MathUtils.ceilByValue(estimatedCash, 5.0)
        }

        cash = estimatedCash
	updateMessageContent()

	initialized = true
    }

    fun updateMessageContent() {
        val message = StringBuilder()

        if (type == Type.BAG_HIGH_QUALITY) {
            message.append("💋 Installment top grade bag 💋\n\n")
        }

        message.append(name)
            .append(
                if(type == Type.PHONE) ". Refurbished unit.\uD83D\uDCF1\uD83D\uDCF1 No warranty mga frenny. ‼️\n"
                else "\n"
            )

        if(caption != null) message.append("${caption}\n")
        if(pieces != null) {
		message.append("#️⃣ ${pieces!!} ${try {
			if(pieces!!.toInt() > 1) {
				"PCs"
			} else {
				"PC"
			}
		} catch (nfe : NumberFormatException) {
			"PC(s)"
		}}\n")
	}
        if(noDesign) message.append("❎ No choosing design ❎\n")

	if(colors.size > 0) {
		message.append("🌈 Available colors:\n")
		colors.forEach { color ->
			message.append("   $color\n")
		}
		message.append('\n')
	}

	if(designs.size > 0) {
		message.append("🎀 Available designs:\n")
		designs.forEach { designs ->
			message.append("   $designs\n")
		}
		message.append('\n')
	}

	if(includes.size > 0) {
		message.append("👉 Including:\n")
		includes.forEach { item ->
			message.append("   $item\n")
		}
		message.append('\n')
	}


        if(isCashOnly) {
		if(size != null) {
                	message.append("\n📐 Size: $size")
        	}
            message.append(String.format("\n\uD83D\uDCB5 Cash Only: P%,d\n", cash.toInt()))
        } else {
            message.append("\n✨ Installment Info ✨")

            message.append(String.format("\n⬇️ Down: %,d", downPayment.toInt()))
                .append(String.format("\n🪙 %,d/week", weeklyPayment.toInt()))
                .append("\n🕒 $length weeks to pay")
                .append(String.format("\n💵 Cash: %,d\n", cash.toInt()))
	    if(size != null) {
                message.append("📐 Size: $size\n")
	    }
        }


        if(type == Type.DEFAULT || type == Type.HOUSE_ITEM || type == Type.PHONE) {
            message.append(  "\n✅ - Free delivery(nearby areas)/meet up")
                       .append("\n✅ - Cash/gcash accepted")
                       .append("\n✅ - Goodpayer only please. 🤗")
                       .append("\n✅ - Always refer sa latest post for updated prices")
        }

	if(isCashOnly) {
		message.append("\n\n✨ Items na abot kaya, pili kana 😉✨")
	} else {
        	when (type) {
            		Type.CHEAP_HOUSE_ITEM, Type.HOUSE_ITEM -> message.append(
                	"\n\n✨ Para makapagpundar ng 'di nabibigatan, gawin nating hulugan. ✨"
            		)
            		Type.BAG_LOW_QUALITY, Type.BAG_HIGH_QUALITY, Type.SHOES_SLIPPERS, Type.CLOTHES -> message.append(
                	"\n\n✨ Mukang mayaman kahit nag hulugan 😉 ✨"
            		)
        	}
	}

        messageContent = message.toString()
    }

    fun getMessageContent() : String {
	if(!initialized) {
		println("WARNING: an Item's getMessageContent() has been called without calling updateFromSupplierPrice()" +
			"first.")
	}

	return messageContent
    }

    fun setType(type : String) : Item {
        this.type = type
        return this
    }

    fun setSize(size : String?) : Item {
        this.size = size
        return this
    }

    fun setCaption(caption : String?) : Item {
        this.caption = caption
        return this
    }

    fun setMyDayCaption(myDayCaption : String?) : Item {
        this.myDayCaption = myDayCaption

        return this
    }

    fun setCaptionInMyDay(captionInMyDay : Boolean) : Item {
	    this.captionInMyDay = captionInMyDay
	    return this
    }

    fun setPostInfoData(data : FacebookScraper.Companion.PostInfo) : Item {
       this.data = data

       return this
    }

    fun setSizeInMyDay(sizeInMyDay : Boolean) : Item {
	this.sizeInMyDay = sizeInMyDay

	return this
    }

    fun addAttachments(attachments : List<String>?) : Item {
        if(attachments != null) data?.attachments?.addAll(attachments)
        return this
    }

    fun addAttachments(attachments : ArrayList<String>?) : Item {
        if(attachments != null) data?.attachments?.addAll(attachments)

        return this
    }

    override fun hashCode() : Int {
	var fullString = messageContent

	if(data != null) {
		for(str in data?.attachments!!) {
			fullString += "\n" + str
		}
	}

	return fullString.hashCode()
     }

}
