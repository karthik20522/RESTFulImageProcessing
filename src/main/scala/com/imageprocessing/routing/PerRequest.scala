package com.imageprocessing.routing

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write }
import spray.http.StatusCode
import com.imageprocessing._
import com.imageprocessing.routing.PerRequest._
import spray.http.StatusCodes._
import spray.http.MediaTypes.{ `image/jpeg`, `application/json` }
import spray.http.HttpEntity
import spray.http.ChunkedResponseStart
import spray.http.HttpResponse
import spray.http.ContentType
import spray.routing.directives.RespondWithDirectives._
import spray.http.MediaType

trait PerRequest extends Actor with Json4sSupport {

  import context._

  val json4sFormats = DefaultFormats
  implicit val formats = Serialization.formats(NoTypeHints)

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(20.seconds)
  target ! message

  def receive = {
    case img: Array[Byte] => {
      val entity = HttpEntity(`image/jpeg`, img)
      r.responder ! HttpResponse(OK, entity)
      stopActor
    }
    case rest: RestMessage => {
      val entity = HttpEntity(`application/json`, pretty(render(parse(write(rest)))))
      r.responder ! HttpResponse(OK, entity)
      stopActor
    }
    case ReceiveTimeout => {
      r.complete(GatewayTimeout, Error("Request timeout"))
      stopActor
    }
    case _ => {
      r.complete(InternalServerError, Error("Internal Server Error"))
      stopActor
    }
  }

  def stopActor = {
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        r.complete(InternalServerError, Error("Internal Server Error"))
        Stop
      }
    }
}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, message)))
}