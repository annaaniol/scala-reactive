package eShop

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.Logging

import scala.concurrent.duration._
import eShop._

class Checkout() extends Actor with Timers {
  val log = Logging(context.system, this)

  def receive = selectingDelivery

  var remoteCustomer :ActorRef = null
  var remoteCart :ActorRef = context.parent

  timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)

  def selectingDelivery: Receive = {
    case CheckoutCancelled() =>
      log.info("Checkout cancelled")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case DeliverySelected() =>
      remoteCustomer = sender
      log.info("Delivery type selected")
      context.become(selectingPaymentMethod)
    case CheckoutTimeout() =>
      log.info("Timeout in selectingDelivery! Checkout cancelled")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      log.warning("Failed in selectingDelivery. Unhandled message: " + msg)
  }

  def selectingPaymentMethod: Receive = {
    case PaymentSelected() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
      val paymentServiceActor = context.actorOf(Props(new PaymentService(remoteCustomer)), "paymentServiceActor")
      remoteCustomer ! PaymentServiceStarted(paymentServiceActor)
      log.info("Payment method selected")
      context.become(processingPayment)
    case CheckoutCancelled() =>
      log.info("Checkout cancelled on selectingPaymentMethod")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case CheckoutTimeout() =>
      log.info("Timeout in selectingPaymentMethod")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      log.warning("Failed in selectingPaymentMethod. Unhandled message: " + msg)
  }

  def processingPayment: Receive = {
    case PaymentReceived() =>
      timers.cancel(PaymentTimerKey)
      log.info("Payment received")
      remoteCart ! CheckoutClosed()
      context.stop(self)
    case PaymentTimeout() =>
      log.info("Timeout in processingPayment")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case CheckoutCancelled() =>
      log.info("Checkout cancelled on processingPayment")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      log.warning("Failed in processingPayment. Unhandled message: " + msg)
  }
}
