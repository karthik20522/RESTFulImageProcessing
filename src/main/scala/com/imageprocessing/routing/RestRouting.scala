package com.imageprocessing.routing

import akka.actor.{ Props, Actor }
import com.imageprocessing._
import spray.routing.{ Route, HttpService }
import com.imageprocessing.core.ProcessImageActor

class RestRouting extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val route = {
    get {
      path(Segment / Rest) { (id, pathRest) =>
        processImageRoute {
          Request(id, pathRest)
        }
      }
    }
  }

  def processImageRoute(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new ProcessImageActor()), message)
}
