package com.imageprocessing

import akka.io.IO
import spray.can.Http

import akka.actor.{ Props, ActorSystem }
import com.imageprocessing.routing.RestRouting

object Boot extends App {
  implicit val system = ActorSystem("imageProcessing-demo")

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("RESTful Image Prceossing actor shutdown.")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 8081)
}