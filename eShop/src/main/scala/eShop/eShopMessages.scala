package eShop

import akka.actor.ActorRef

object eShopMessages {

  case class ShowState() {

  }

  case class ItemRemoved(item: String) {

  }
  case class CheckoutClosed() {

  }
  case class CheckoutCancelled() {

  }
  case class CartTimeout() {

  }
  case class CheckoutTimeout() {

  }
  case class Canceled() {

  }
  case class DeliverySelected() {

  }
  case class PaymentSelected() {

  }
  case class PaymentSelectedWithProvider(paymentProvider: String) {

  }
  case class PaymentReceived() {

  }
  case class PaymentTimeout() {

  }

  case class DoPayment() {

  }

  case class PaymentConfirmed() {

  }

  case class CartEmpty() {

  }

  case class CheckoutStarted(checkout: ActorRef) {

  }

  case class StartCheckout() {

  }

  case class AddItem(item: String) {

  }

  case class PaymentServiceStarted(paymentService: ActorRef) {

  }

  case class Start() {

  }

  case object CheckoutTimerKey
  case object PaymentTimerKey
  case object CartTimerKey

  case object Done
  case object Failed
  case object Init
}
