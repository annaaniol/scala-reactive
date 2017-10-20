package eShop

import akka.actor.{Actor, Timers, ActorRef}

import scala.concurrent.duration._
import eShop._

class Checkout extends Actor with Timers {

  def receive = awaiting

  var remoteCart :ActorRef = null

  def awaiting: Receive = {
    case CheckoutStarted() =>
      remoteCart = sender
      println("Checkout started")
      timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)
      context.become(selectingDelivery)
    case msg =>
      println("Failed in awaiting. Unhandled message: " + msg)
  }

  def selectingDelivery: Receive = {
    case CheckoutCancelled() =>
      println("Checkout cancelled")
      timers.cancel(CheckoutTimerKey)
      remoteCart ! CheckoutCancelled()
      context.become(awaiting)
    case DeliverySelected() =>
      println("Delivery type selected")
      context.become(selectingPaymentMethod)
    case CheckoutTimeout() =>
      println("Timeout in selectingDelivery! Checkout cancelled")
      remoteCart ! CheckoutCancelled()
      context.become(awaiting)
    case msg =>
      println("Failed in selectingDelivery. Unhandled message: " + msg)
      context.become(awaiting)
  }

  def selectingPaymentMethod: Receive = {
    case PaymentSelected() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
      println("Payment method selected")
      context.become(processingPayment)
    case CheckoutCancelled() =>
      println("Checkout cancelled on selectingPaymentMethod")
      timers.cancel(CheckoutTimerKey)
      remoteCart ! CheckoutCancelled()
      context.become(awaiting)
    case PaymentTimeout() =>
      println("Timeout in selectingPaymentMethod")
      remoteCart ! CheckoutCancelled()
      context.become(awaiting)
    case msg =>
      println("Failed in selectingPaymentMethod. Unhandled message: " + msg)
      context.become(awaiting)
  }

  def processingPayment: Receive = {
    case PaymentReceived() =>
      timers.cancel(PaymentTimerKey)
      println("Payment received")
      remoteCart ! CheckoutClosed()
      context.become(awaiting)
    case PaymentTimeout() =>
      println("Timeout in processingPayment")
      remoteCart ! CheckoutCancelled()
      context.become(awaiting)
    case msg =>
      println("Failed in processingPayment. Unhandled message: " + msg)
      context.become(awaiting)
  }

}
