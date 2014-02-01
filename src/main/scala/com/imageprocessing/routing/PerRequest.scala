package com.imageprocessing.routing

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import spray.http.StatusCode
import com.imageprocessing._
import com.imageprocessing.routing.PerRequest._
import spray.http.StatusCodes._
import spray.http.MediaTypes.`image/jpeg`
import spray.http.HttpEntity
import spray.http.ChunkedResponseStart
import spray.http.HttpResponse

trait PerRequest extends Actor with Json4sSupport {

  import context._

  val json4sFormats = DefaultFormats

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(20.seconds)
  target ! message

  def receive = {
    case img: Array[Byte] => complete(OK, img)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request timeout"))
    case _ => complete(InternalServerError, Error("Request timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    status match {
      case OK => {
        val entity = HttpEntity(`image/jpeg`, obj.asInstanceOf[Array[Byte]])
        r.responder ! HttpResponse(entity = entity)
      }
      case _ => r.complete(status, obj)
    }

    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
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