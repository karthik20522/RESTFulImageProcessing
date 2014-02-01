package com.imageprocessing

import com.sksamuel.scrimage.Image

trait RestMessage

case class Request(id: String, restOperations: String) extends RestMessage

case class ProcessImage(image: Image, operations: Map[String, String])

case class Error(message: String)