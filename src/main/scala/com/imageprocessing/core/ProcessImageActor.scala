package com.imageprocessing.core

import akka.actor.{ ActorRef, Actor }
import akka.actor.SupervisorStrategy.Escalate
import com.imageprocessing._
import scala.Some
import akka.actor.OneForOneStrategy
import com.sksamuel.scrimage
import scala.concurrent.Future
import dispatch.Defaults.executor
import dispatch._
import com.github.nscala_time.time.Imports._
import java.io.InputStream
import java.io.ByteArrayInputStream
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter._
import java.awt.Color

class ProcessImageActor() extends Actor {

  def receive = {

    /**
     * Download file and extract operations to be performed
     */
    case Request(id, operations) => {
      downloadFileFromURL(id).map { imageBytes =>
        {
          val image = Image(imageBytes)
          val operationList = getOperationMap(operations.split("/").toList, Map())
          self ! ProcessImage(image, operationList)
        }
      }
    }

    /**
     * *
     * Do Image processing
     */
    case ProcessImage(image, operations) => {
      operations.isEmpty match {
        case true => {
          context.parent ! image.write
        }
        case false => {
          val (key, value) = operations.head
          val processedImage = processImage(image, key, value)
          self ! ProcessImage(processedImage, operations.tail)
        }
      }
    }
  }

  /**
   *
   * Do Image Processing
   */
  def processImage(image: Image, op: String, values: String) = Image {
    val params = values.split(",")
    op.toLowerCase() match {
      case "f" => {
        val filterParams = params(0).split(":")
        filterParams(0).toLowerCase match {
          case "blur" => image.filter(BlurFilter)
          case "border" => image.filter(BorderFilter(filterParams(1).toInt))
          case "brightness" => image.filter(BrightnessFilter(filterParams(1).toFloat))
          case "bump" => image.filter(BumpFilter)
          case "chrome" => image.filter(ChromeFilter())
          case "color_halftone" => image.filter(ColorHalftoneFilter())
          case "contour" => image.filter(ContourFilter())
          case "contrast" => image.filter(ContrastFilter(filterParams(1).toFloat))
          case "despeckle" => image.filter(DespeckleFilter)
          case "diffuse" => image.filter(DiffuseFilter(filterParams(1).toInt))
          case "dither" => image.filter(DitherFilter)
          case "edge" => image.filter(EdgeFilter)
          case "emboss" => image.filter(EmbossFilter)
          case "errordiffusion" => image.filter(ErrorDiffusionHalftoneFilter())
          case "gamma" => image.filter(GammaFilter(filterParams(1).toInt))
          case "gaussian" => image.filter(GaussianBlurFilter())
          case "glow" => image.filter(GlowFilter())
          case "grayscale" => image.filter(GrayscaleFilter)
          case "hsb" => image.filter(HSBFilter(filterParams(1).toInt))
          case "invert" => image.filter(InvertFilter)
          case "lensblur" => image.filter(LensBlurFilter())
          case "lensflare" => image.filter(LensFlareFilter)
          case "minimum" => image.filter(MinimumFilter)
          case "maximum" => image.filter(MaximumFilter)
          case "motionblur" => image.filter(MotionBlurFilter(Math.PI / filterParams(1).toInt, filterParams(2).toInt))
          case "noise" => image.filter(NoiseFilter())
          case "offset" => image.filter(OffsetFilter(filterParams(1).toInt, filterParams(2).toInt))
          case "oil" => image.filter(OilFilter())
          case "pixelate" => image.filter(PixelateFilter(filterParams(1).toInt))
          case "pointillize_square" => image.filter(PointillizeFilter(PointillizeGridType.Square))
          case "posterize" => image.filter(PosterizeFilter())
          case "prewitt" => image.filter(PrewittFilter)
          case "quantize" => image.filter(QuantizeFilter(filterParams(1).toInt))
          case "rays" => image.filter(RaysFilter(threshold = filterParams(1).toFloat, strength = filterParams(2).toFloat))
          case "ripple" => image.filter(RippleFilter(RippleType.Sine))
          case "roberts" => image.filter(RobertsFilter)
          case "rylanders" => image.filter(RylandersFilter)
          case "sepia" => image.filter(SepiaFilter)
          case "smear_circles" => image.filter(SmearFilter(SmearType.Circles))
          case "snow" => image.filter(SnowFilter)
          case "sobels" => image.filter(SobelsFilter)
          case "solarize" => image.filter(SolarizeFilter)
          case "sparkle" => image.filter(SparkleFilter())
          case "summer" => image.filter(SummerFilter())
          case "swim" => image.filter(SwimFilter())
          case "television" => image.filter(TelevisionFilter)
          case "threshold" => image.filter(ThresholdFilter(filterParams(1).toInt))
          case "tritone" => image.filter(TritoneFilter(new Color(0xFF000044), new Color(0xFF0066FF), Color.WHITE))
          case "twirl" => image.filter(TwirlFilter(filterParams(1).toInt))
          case "unsharp" => image.filter(UnsharpFilter())
          case "vignette" => image.filter(VignetteFilter())
          case "vintage" => image.filter(VintageFilter)
        }
      }
      case "autocrop" => image.autocrop(java.awt.Color.getColor(params(0)))
      case "trim" => image.trim(params(0).toInt, params(1).toInt, params(2).toInt, params(3).toInt)
      case "fit" => image.fit(params(0).toInt, params(1).toInt)
      case "bound" => image.bound(params(0).toInt, params(1).toInt)
      case "cover" => image.cover(params(0).toInt, params(1).toInt)
      case "pad" => params.length match {
        case 1 => image.pad(params(0).toInt)
        case _ => image.padTo(params(0).toInt, params(1).toInt)
      }
      case "rotate" => params(0).toLowerCase() match {
        case "left" => image.rotateLeft
        case _ => image.rotateRight
      }
      case "flip" => params(0).toLowerCase() match {
        case "x" => image.flipX
        case _ => image.flipY
      }
      case "crop" =>
        params.length match {
          case 1 => image.resize(params(0).toInt)
          case _ => image.resizeTo(params(0).toInt, params(1).toInt)
        }
      case "resize" =>
        params.length match {
          case 1 => image.scale(params(0).toInt)
          case _ => image.scaleTo(params(0).toInt, params(1).toInt)
        }
    }
  }

  /**
   * *
   * Convert REST image operations to MAP
   */
  def getOperationMap(ops: List[String], map: Map[String, String]): Map[String, String] = ops match {
    case Nil => map
    case _ => {
      val key = ops.head
      val value = ops.tail.head
      val rest = ops.tail.tail
      getOperationMap(rest, map + (key -> value))
    }
  }

  /**
   * Download file from URL
   */
  def downloadFileFromURL(id: String): Future[Array[Byte]] = {
    //val folderName = s"${DateTime.now.year.get}0${DateTime.now.month.get}${DateTime.now.day.get}"
    val req = url(s"http://farm8.staticflickr.com/7451/11994271374_8bd853ef41_h.jpg").GET
    Http(req OK as.Bytes)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}