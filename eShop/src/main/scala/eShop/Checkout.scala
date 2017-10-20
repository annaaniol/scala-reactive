package eShop

import akka.actor.{Actor, Timers, ActorRef}

import scala.concurrent.duration._
import eShop._

class Checkout extends Actor with Timers {

  def receive = selectingDelivery

  var remoteCart :ActorRef = context.parent

  timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)

  def selectingDelivery: Receive = {
    case CheckoutCancelled() =>
      println("Checkout cancelled")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case DeliverySelected() =>
      println("Delivery type selected")
      context.become(selectingPaymentMethod)
    case CheckoutTimeout() =>
      println("Timeout in selectingDelivery! Checkout cancelled")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      println("Failed in selectingDelivery. Unhandled message: " + msg)
  }

  def selectingPaymentMethod: Receive = {
    case PaymentSelected() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
      println("Payment method selected")
      context.become(processingPayment)
    case CheckoutCancelled() =>
      println("Checkout cancelled on selectingPaymentMethod")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case PaymentTimeout() =>
      println("Timeout in selectingPaymentMethod")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      println("Failed in selectingPaymentMethod. Unhandled message: " + msg)
  }

  def processingPayment: Receive = {
    case PaymentReceived() =>
      timers.cancel(PaymentTimerKey)
      println("Payment received")
      remoteCart ! CheckoutClosed()
      context.stop(self)
    case PaymentTimeout() =>
      println("Timeout in processingPayment")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case msg =>
      println("Failed in processingPayment. Unhandled message: " + msg)
  }
}
