package eShop

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives.public
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.pattern.pipe
import akka.util.ByteString

import scala.concurrent.Await
import scala.concurrent.duration._
import eShopMessages._

class PaymentService(paymentProvider: String) extends Actor
  with Timers with ActorLogging {

  def this(paymentProvider: String, customer: ActorRef, checkout: ActorRef) = {
    this(paymentProvider)
    this.remoteCustomer=customer
    this.remoteCheckout=checkout
  }
  def this() = {
    this("none")
  }

  import context.dispatcher
  import akka.pattern.pipe

  var remoteCheckout :ActorRef = _
  var remoteCustomer :ActorRef = _

  if(remoteCustomer==null) remoteCheckout = context.parent

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  override def preStart() = {
    http.singleRequest(HttpRequest(uri = "http://localhost:8080/visa"))
      .pipeTo(self)
  }


  override def receive = {
    case eShopMessages.DoPayment() =>
      println("PaymentService: Payment done")
      remoteCustomer = context.sender
      remoteCustomer ! PaymentConfirmed()
      remoteCheckout ! PaymentReceived()
      context.stop(self)
    case resp @ HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        println("Got response, body: " + body.utf8String)
        resp.discardEntityBytes()
        shutdown()
      }
    case resp @ HttpResponse(code, _, _, _) =>
      println("Request failed, response code: " + code)
      resp.discardEntityBytes()
      shutdown()
    case msg =>
      log.info("Unhandled message: " + msg)
  }

  def shutdown() = {
    Await.result(http.shutdownAllConnectionPools(),Duration.Inf)
    context.system.terminate()
  }


}
