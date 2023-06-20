package collage

import Item
import internals.Constants
import sun.font.AttributeMap
import sun.font.FontLineMetrics
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Stroke
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.max
import kotlin.math.round


/* This is the class where I would be making my collage
* TODO: Adjust the width and height of the canvas depending on the amount
*  of the image in a collage. */
class Collage {
      companion object {

          /* To be able to obtain font metrics data for string calculations */

          private const val SCALE = 10f
          private const val GAPS = 15
          private const val IMG_SIZE = 500
	  private const val SCALE_MULTIPLIER = 4f

	  /* should I implement hypenation? */
	  private const val MAX_CHAR_WIDTH = 18

	  /*
          @JvmStatic
          fun main(args: Array<String>) {

              val frame = JFrame("Test frame")
              frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

              val panel = JPanel()

              val src = ArrayList<BufferedImage>()
              var count = 1
              for(i in 1 .. 2) {
                  if(i == 9) count = 1
                  src.add(ImageIO.read(File(Constants.HOME, "Pictures/Tester/${count}.png")))
                  count++
              }

              val item = Item("Test Item", 470.0)
              item.images.addAll(src)
              /* val collage = createStoryFromItem(item, true, "NotoColorEmoji") */
              val icon = ImageIcon(collage)
              val imageLabel = JLabel(icon)

              panel.add(imageLabel)

              frame.contentPane = panel
              frame.setLocationRelativeTo(null)
              frame.setSize(140, 80)
              frame.title = "Floating Wndw"
              frame.pack()
              SwingUtilities.invokeLater() {
                  frame.isVisible = true
              }
          }
*/

          fun createStoryFromItem(item : Item, adjustImage : Boolean, font : String, columns : Int) : BufferedImage {
              var text = StringBuilder()

	      if(item.captionInMyDay) {
		      appendLimitedTextToStringBuilder(item.caption!!, text)
		      text.append('\n')
	      }

	      if(item.myDayCaption != null) {
		      appendLimitedTextToStringBuilder(item.myDayCaption!!, text)
		      text.append('\n')
	      }

		if(item.piecesInMyDay && item.pieces != null) {
			text.append("#️⃣ ${item.pieces} ${try {
				if(item.pieces!!.toInt() > 1) {
					"PCs"
				} else {
					"PC"
				}
			} catch (nfe : NumberFormatException) {
				"PC(s)"
			}}\n")
		}
	      if(item.sizeInMyDay && item.size != null) {
	      		appendLimitedTextToStringBuilder("📐 Size: ${item.size}\n", text)
	      }

	       if(item.noDesign) {
		       text.append("❎ No choosing design ❎\n")
	       }

	       if(item.colorsInMyDay) {
			if(item.colors.size > 0) {
				text.append("🌈 Available colors:\n")
				item.colors.forEach { color ->
					appendLimitedTextToStringBuilder("   $color\n", text)
				}
				text.append('\n')
			}
		}

		if(item.designsInMyDay) {
			if(item.designs.size > 0) {
				text.append("🎀 Available designs:\n")
				item.designs.forEach { design ->
					/* TODO: VERY Experimental, consider not using it on
					* text that couldn't really be expected to be long
					* so time in testing won't be so long. (These are one
					* of those */
					appendLimitedTextToStringBuilder("   $design\n", text)
				}
				text.append('\n')
			}
		}

		if(item.includesInMyDay) {
			if(item.includes.size > 0) {
				text.append("👉 Including:\n")
				item.includes.forEach { i ->
					appendLimitedTextToStringBuilder("   $i\n", text)
				}
				text.append('\n')
			}
		}

              text.append(if(item.isCashOnly) {
                  String.format("💵 Cash: %,d", item.cash.toInt())
              } else {
                String.format("\uD83D\uDC47 Down: %,d\n", item.downPayment.toInt()) +
                          String.format("\uD83D\uDCB0 %,d/weekly\n", item.weeklyPayment.toInt()) +
                          "\uD83D\uDD52 ${item.length.toInt()} weeks to pay\n" +
                          String.format("\uD83D\uDCB5 Cash: %,d", item.cash.toInt())
              })

              return makeCollage(item.images, adjustImage, text.toString(), font, GAPS, SCALE, columns)
          }

          /* Automatically adjusts the width and height to make neat-looking pictures */
          fun makeCollage(src : ArrayList<BufferedImage>, adjustImage : Boolean, caption : String, font : String, gaps : Int, scale : Float, presetCols : Int) : BufferedImage {
              val columns = if (presetCols > 0) {
	           presetCols
	      } else {
		      when(src.size) {
             		1 -> 1
             		2 -> 2
             		3 -> 2
             		4 -> 3
             		5 -> 3
             		6 -> 3
             		7 -> 3
             		8 -> 3
             		9 -> 4
             		10 -> 4
             		11 -> 4
             		12 -> 4
             		13 -> 4
             		14 -> 5
             		15 -> 5
             		16 -> 5
             		17 -> 5
             		else -> 6
		  }
              }

	      /* TODO: Make a switch for the collage to adapt to the images' sizes. This is just a temporary patch.
	      * Workaround for Ella Sotto's images, sometimes they are collaged in a way that will make the details
	      * most likely be obscure by the aspect ratio crop in the default setting. So, instead of using constants
	      * just use the image's dimensions as default if it's just a single image. Though I should implement max adaption
	      */

	      /* For now, this is just we will do, the adjustImage variable in generateCollage is still work in
	      * progress, so I have to make do with this for now that it will only work on single images with specific
	      * dimensions */
	      var collage = generateCollage(src, IMG_SIZE, IMG_SIZE, 15, columns, adjustImage)

              if(collage.width < 800 || collage.height < 800) {
                  val temp = BufferedImage((collage.width * SCALE_MULTIPLIER).toInt(), (collage.height * SCALE_MULTIPLIER).toInt(), BufferedImage.TYPE_4BYTE_ABGR)
		  val tempG = temp.graphics as Graphics2D
		  tempG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			  	RenderingHints.VALUE_ANTIALIAS_ON)
                  tempG.drawImage(collage, 0, 0, temp.width, temp.height, 0, 0, collage.width, collage.height, null)
		  tempG.dispose()
                  collage.flush()

                  collage = temp
              }

	      // :: I end up with large file sizes ....
	      val size = (collage.data.dataBuffer.size / 4L);

	      if(size > 60480) {
	     	 val adjWidth = collage.width / 2;
	     	 val adjHeight = collage.height / 2;

	     	 val adjCollage = BufferedImage(adjWidth, adjHeight, BufferedImage.TYPE_3BYTE_BGR);
	     	 val adjG = adjCollage.graphics as Graphics2D;
	     	 adjG.drawImage(collage, 0, 0, adjWidth, adjHeight, 0, 0, collage.width, collage.height, null);

	     	 adjG.dispose();
	     	 collage.flush();

	     	 collage = adjCollage;
	      }

              val text = getTextAsImage(collage, caption, gaps, scale, null, Color.BLACK, font)

              val textScale = 0.4

		val widthToHeightRatio = text.height.toDouble()/text.width.toDouble()
	      /* val heightToWidthRatio = text.width.toDouble()/text.height.toDouble() */
              val tW = collage.width * textScale
              val tH = max(collage.height * 0.2, tW * (widthToHeightRatio)).toInt()

              /* Just a test */
	      val cX = collage.width - tW.toInt() + gaps * 2
	      val cY = collage.height - tH + gaps * 2
	      val cg = collage.graphics as Graphics2D

              cg.drawImage(text, cX, cY, collage.width, collage.height, 0, 0, text.width, text.height, null)
              // cg.drawImage(text, cX, cY, collage.width, collage.height, 0, 0, text.width, text.height, null)

	      cg.color = Color.BLACK

	      /* This sort of disappear when the image is too big */
	      cg.stroke = BasicStroke(2.5f)
	      cg.drawRect(cX, cY, collage.width - cX, collage.height - cY)

	      cg.dispose()
              text.flush()

              return collage
          }

          private fun generateCollage(src : List<BufferedImage>, imageWidth : Int, imageHeight : Int,
                                      gaps : Int, columns : Int, adaptToImageDimensions : Boolean) : BufferedImage {
	      var stitchImage : BufferedImage

              val finalColumns = if(src.size < columns) {
                  src.size
              } else {
                  columns
              }

              val rows = ceil(src.size / finalColumns.toDouble()).toInt()

	      if(adaptToImageDimensions) {
		// The dimensions! the s*Height has borders already set
		var stitchWidth = 0
		var stitchHeight = gaps * (rows + 1)

		val rowMaxHeights = IntArray(rows) { 0 }
		val columnsMaxWidth = IntArray(rows) { 0 }

		var highestRowWidth = 0
		for (r in 0 until rows) {
			var rowWidth = 0
			var rowHighestHeight = 0

			for(x in 0 until (finalColumns + min(src.size - (r + 1) * finalColumns, 0))) {
				val srcImg = src[finalColumns * r + x]
				val arPortraitW = srcImg.height.toDouble()/srcImg.width
				val arPortraitH = srcImg.width.toDouble()/srcImg.height

				val arLandscapeW = srcImg.width.toDouble()/srcImg.height
				val arLandscapeH = srcImg.height.toDouble()/srcImg.width

				val isPortrait = srcImg.width < srcImg.height

				val srcWidthAdjusted = (imageWidth * (if(isPortrait) arPortraitW else arLandscapeW).toDouble()).toInt()
				val srcHeightAdjusted = (imageHeight * (if(isPortrait) arPortraitH else arLandscapeH).toDouble()).toInt()
				rowWidth += srcWidthAdjusted + gaps

				if(srcHeightAdjusted > rowHighestHeight) rowHighestHeight = srcHeightAdjusted
				if(columnsMaxWidth[x] < srcWidthAdjusted) columnsMaxWidth[x] = srcWidthAdjusted

				if(highestRowWidth < rowWidth) highestRowWidth = rowWidth
			}

			if(rowWidth > stitchWidth) stitchWidth = highestRowWidth + gaps

			rowMaxHeights[r] = rowHighestHeight
			stitchHeight += rowMaxHeights[r]
		}

		stitchImage = BufferedImage(stitchWidth, stitchHeight, BufferedImage.TYPE_INT_ARGB)
		val stitchGraphics = stitchImage.createGraphics()

		stitchGraphics.color = Color.WHITE
		stitchGraphics.fillRect(0, 0, stitchWidth, stitchHeight)

		stitchGraphics.color = Color.BLACK
		stitchGraphics.drawRect(0, 0, stitchWidth - 1, stitchHeight - 1)

		/** Strive to center the rows and also I guess the images vertical wise **/
		var index = 0
		var currentY = gaps

		for(y in 0 until rows) {
			val rowHighestHeight = rowMaxHeights[y]
			var curX = 0

			for(x in 0 until finalColumns + min(src.size - (y + 1) * finalColumns, 0)) {
					val srcImg = src[index]

					val srcWidthAdjusted  = (imageWidth * (srcImg.width.toDouble()/srcImg.height)).toInt()
					val srcHeightAdjusted = (imageHeight * (srcImg.height.toDouble()/srcImg.width)).toInt()

					var dstX = curX + (columnsMaxWidth[x] - srcWidthAdjusted)/2 + gaps// (stitchWidth - rowMaxWidth) / 2 + gaps * 2 + (gaps * x - gaps)
					val padY = ((rowHighestHeight - srcHeightAdjusted)/2)

					stitchGraphics.drawImage(srcImg, dstX, currentY + padY, srcWidthAdjusted, srcHeightAdjusted, null)
				        stitchGraphics.stroke = BasicStroke(5f)
				        stitchGraphics.color = Color.BLACK
				        stitchGraphics.drawRect(dstX, currentY + padY, srcWidthAdjusted, srcHeightAdjusted)

					curX += dstX + srcWidthAdjusted
					index++
			}

			currentY += rowHighestHeight + gaps
		}
	      } else {
		val stitchWidth = ((gaps + imageWidth) * finalColumns) + gaps
		val stitchHeight = ((gaps + imageHeight) * rows) + gaps

		stitchImage = BufferedImage(stitchWidth, stitchHeight, BufferedImage.TYPE_INT_ARGB)
		val stitchGraphics = stitchImage.createGraphics()

		stitchGraphics.color = Color.WHITE
		stitchGraphics.fillRect(0, 0, stitchWidth, stitchHeight)

		stitchGraphics.color = Color.BLACK
		stitchGraphics.drawRect(0, 0, stitchWidth - 1, stitchHeight - 1)

		var index = 0
		for(y in 0 until rows) {
			val iY = y * (imageHeight + gaps) + gaps
			for(x in 0 until finalColumns) {
			    val iX = x * (imageWidth + gaps) + gaps;

			    if(index < src.size) {
			        /* TODO: Use black bars or colored bars for images */
			        val srcImg = src[index]
			        /* NOW LET'S DO SUM MATHS! */
			        var srcX : Int
			        var srcY : Int
			        var cropWidth : Int
			        var cropHeight : Int

			        if(srcImg.width > srcImg.height) {
			            cropWidth = srcImg.height
			            cropHeight = srcImg.height
			            srcX = (srcImg.width - cropWidth) / 2
			            srcY = 0
			        } else {
			            cropWidth = srcImg.width
			            cropHeight = srcImg.width
			            srcX = 0
			            srcY = (srcImg.height - cropHeight) / 2
			        }

			        stitchGraphics.drawImage(srcImg, iX, iY, iX + imageWidth, iY + imageHeight,
			            srcX, srcY, srcX + cropWidth, srcY + cropHeight, null)

			        stitchGraphics.stroke = BasicStroke(5f)
			        stitchGraphics.color = Color.BLACK
			        stitchGraphics.drawRect(iX, iY, imageWidth, imageHeight)
			        index++
			    }
			}
		}
              }
		return stitchImage
          }

          /* TODO: Figure out to make an effective font size
          *  TODO: For some reason, when I use this to generate
          *   text with emoji's for the story files, it -- that
          *   is the emoji -- doesn't show up. Emoji's doesn't
          *   show up in OpenJDK */
          private fun getTextAsImage(collage : BufferedImage, text : String, gaps : Int, scale : Float, bg : Color?, fg : Color, fontName : String) : BufferedImage {
	      val collageGraphics = collage.graphics as Graphics2D

	      val attr = hashMapOf<TextAttribute, Any>()
              attr[TextAttribute.FAMILY] = fontName
              attr[TextAttribute.SIZE] = (collageGraphics.font.size * scale).toInt()
              attr[TextAttribute.WEIGHT] = 2f

              val font = Font.getFont(attr)

              collageGraphics.font = font
              val width = getStringLongestLength(text, collageGraphics.getFontMetrics(font))
              val height = getTotalStringHeight(text, font, collageGraphics.fontRenderContext)

              val texts = text.split('\n')

              val w = width + gaps * 3
              val h = (height + gaps * 3).toInt()

	      val canvas = BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR)
              val graphics = canvas.graphics as Graphics2D

              graphics.color = bg?: Color.WHITE
              graphics.fillRect(0, 0, canvas.width, canvas.height)

              graphics.font = font
              graphics.color = fg

              var y = 0.0f

              texts.forEach { line ->
                  y += graphics.fontMetrics.getLineMetrics(line, graphics).height
                  graphics.drawString(line, gaps, y.toInt())
              }

	      graphics.color = Color.BLACK
	      graphics.stroke = BasicStroke(5f)
              graphics.drawRect(0, 0, w, h)

              return canvas
          }

          private fun getRandomColor() : Color {
              return Color(
                  (Math.random() * 255).toInt(),
                  (Math.random() * 255).toInt(),
                  (Math.random() * 255).toInt())
          }

         private fun getStringLongestLength(text: String, fontMetrics : FontMetrics) : Int {
              var longestLength = 0
              text.split('\n').forEach {
                  val length = fontMetrics.stringWidth(it).toInt()
                  if(length > longestLength) {
                      longestLength = length
                  }
              }

              return longestLength
          }

	  private fun cutStringToWidth(line : String, maxWidth : Int) : Array<String> {
		/* println("cut_string_info: param: [$line],\n\tline.length: ${line.length},\n\tmax_width: ${maxWidth}") */
		if(line.length > maxWidth) {
			var lastSeperatorIndex = 0
			var currentIndex = 0
			/* TODO: Kinda questionable */
			for(i in line) {
				if(i == ' ' || i == '-' || i == ',' || i == '\t' ||
					i == '\n' || currentIndex == line.length) {
					if(currentIndex > maxWidth) {
						val part_i =  line.substring(0, if(lastSeperatorIndex < 1) currentIndex - 1 else lastSeperatorIndex)
						val part_ii = line.substring(if(lastSeperatorIndex < 1) currentIndex + 1 else lastSeperatorIndex + 1, line.length)
						/* println("seperator: [" + i + "],\n\tpart_i: [$part_i],\n\tpart_ii: [$part_ii]," +
							"\n\tcurrentIndex: [$currentIndex],\n\tlastSeparator: [$lastSeperatorIndex]") */
						if(part_ii.length > maxWidth) {
							val cut = cutStringToWidth(part_ii, maxWidth)
							var temp = arrayOfNulls<String>(cut.size + 1)
							temp[0] = part_i
							// println(cut.size)
							for(idx in 0..cut.size - 1) {
								// println(temp[idx + 1])
								temp[idx + 1] = cut[idx]
							}

							/* Hacky fix :disappointed: */
							return Array<String>(cut.size + 1) { it -> temp[it]!! }
						} else {
							return arrayOf<String>(part_i, part_ii)
						}
					}

					lastSeperatorIndex = currentIndex
				}
				currentIndex++
			}
		}
		return arrayOf<String>(line)
	  }

	  private fun appendLimitedTextToStringBuilder(string : String, buffer : StringBuilder) {
		val split = string.split("\n")
		      for(i in 0..split.size - 1) {
			      val line = split[i]
			      if(line.length > 0) {
					val cutString = cutStringToWidth(line, MAX_CHAR_WIDTH)
					for(x in 0..cutString.size - 1) {
						buffer.append(cutString[x])
						if(i < split.size) buffer.append("\n")
					}
			      }

		      }
	  }

          private fun getTotalStringHeight(text : String, font : Font, fontRenderContext : FontRenderContext) : Float {
              var totalHeight = 0f
              text.split('\n').forEach {
                  totalHeight += font.getLineMetrics(it, fontRenderContext).height
              }

              return totalHeight
          }
      }
}
