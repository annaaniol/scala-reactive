package eShop

import akka.actor.{Actor, Timers, ActorRef}

import scala.concurrent.duration._

class Checkout extends Actor with Timers {
  import eShop._

  def receive = awaiting

  var remoteCart :ActorRef = null

  def awaiting: Receive = {
    case CheckoutStarted() =>
      remoteCart = sender
      println("Checkout started")
      timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)
      context.become(selectingDelivery)
    case _ =>
      println("Failed in awaiting")
  }

  def selectingDelivery: Receive = {
    case CheckoutCancelled() =>
      println("Checkout cancelled")
      timers.cancel(CheckoutTimerKey)
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case DeliverySelected() =>
      println("Delivery type selected")
      context.become(selectingPaymentMethod)
    case CheckoutTimeout() =>
      println("Timeout in selectingDelivery! Checkout cancelled")
      remoteCart ! CheckoutTimeout()
      context.become(awaiting)
    case _ =>
      println("Failed in selectingDelivery")
      context.become(awaiting)
  }

  def selectingPaymentMethod: Receive = {
    case PaymentSelected() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
      println("Payment method selected")
      context.become(processingPayment)
    case PaymentTimeout() =>
      println("Timeout in selectingPaymentMethod! Checkout cancelled")
      remoteCart ! CheckoutTimeout()
      context.become(awaiting)
    case _ =>
      println("Failed in selectingPaymentMethod")
      context.become(awaiting)
  }

  def processingPayment: Receive = {
    case PaymentReceived() =>
      timers.cancel(PaymentTimerKey)
      println("Payment received")
      remoteCart ! CheckoutClosed()
      context.become(awaiting)
    case PaymentTimeout() =>
      println("Timeout in processingPayment! Checkout cancelled")
      remoteCart ! CheckoutTimeout()
      context.become(awaiting)
    case _ =>
      println("Failed in processingPayment")
      context.become(awaiting)
  }

}
