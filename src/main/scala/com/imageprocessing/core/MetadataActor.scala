package com.imageprocessing.core

import akka.actor.{ ActorRef, Actor }
import akka.actor.SupervisorStrategy.Escalate
import com.imageprocessing._
import scala.Some
import akka.actor.OneForOneStrategy
import dispatch._
import scala.concurrent.Future
import dispatch.Defaults.executor
import com.drew.imaging.ImageMetadataReader
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import com.github.nscala_time.time.Imports._
import java.io.File
import scala.collection.JavaConverters._

class MetadataActor() extends Actor {
  def receive = {
    case Request(id, operations) => {
      println("DOWNLOADING FILE")
      downloadFileFromURL(id).map { imageBytes =>
        {
          //REFACTOR THIS - THIS IS BAD
          //NOT SURE HOW TO CONVERT ARRAY[BYTES] TO BUFFEREDINPUTSTREAM FOR METADATA PLUGIN
          //HACK WAY WAS TO SAY THE FILE TO DISK AND READ IT BACK AGAIN FOR METADATA PLUGIN
          val fileName = s"..//${DateTime.now.year.get}0${DateTime.now.month.get}${DateTime.now.day.get}${DateTime.now.hour.get}${DateTime.now.minute.get}${DateTime.now.second.get}.jpeg"
          val fos = new FileOutputStream(fileName);
          fos.write(imageBytes);
          fos.close();

          val jpegFile = new File(fileName); //Java.io.File has no CLOSE method???
          val metadata = ImageMetadataReader.readMetadata(jpegFile);

          var meta = Map[String, String]()
          for (directory <- metadata.getDirectories().asScala) {
            for (tag <- directory.getTags().asScala) {
              if (!tag.getTagName().contains("TRC"))
                meta += (tag.getTagName() -> tag.getDescription())
            }
          }
          context.parent ! ProcessedMetadata(meta)
        }
      }
    }
  }

  def downloadFileFromURL(id: String): Future[Array[Byte]] = {
    val req = url(s"http://static.flickr.com/36/78402072_083e18becc_o_d.jpg").GET
    Http(req OK as.Bytes)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}