package eShop

import akka.actor.ActorRef

object eShop {

  // eShop state management
  case class ShowState() {

  }

  // eShop v1
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
  case class PaymentReceived() {

  }
  case class PaymentTimeout() {

  }

  // eShop v2
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
