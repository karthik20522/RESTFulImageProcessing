package com.imageprocessing

trait RestMessage

case class Request(id: String, restOperations: String) extends RestMessage

case class ProcessImage(operations: Map[String, String])

case class Error(message: String)