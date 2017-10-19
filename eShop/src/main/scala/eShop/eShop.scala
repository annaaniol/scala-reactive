package eShop

import akka.actor.ActorRef

object eShop {
  case class ItemAdded(item: String) {

  }
  case class ItemRemoved(item: String) {

  }
  case class CheckoutStartedCart(remoteCheckout: ActorRef) {

  }
  case class CheckoutStarted() {

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
  case class PaymentReceived() {

  }
  case class PaymentTimeout() {

  }

  case object CheckoutTimerKey
  case object PaymentTimerKey
  case object CartTimerKey

  case object Done
  case object Failed
  case object Init
}
