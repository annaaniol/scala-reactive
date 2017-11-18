package eShop

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.Logging
import eShop._

class Customer extends Actor with Timers {
  val log = Logging(context.system, this)

  var cartActor :ActorRef = null
  var checkoutActor :ActorRef = null
  var paymentServiceActor :ActorRef = null

  def receive = {
    case Start() =>
      cartActor = context.actorOf(Props[CartOld], "cartActor")
    case CartEmpty() =>
      log.info("Customer: Cart is empty")
    case CheckoutStarted(checkout) =>
      checkoutActor = checkout
    case PaymentServiceStarted(paymentService) =>
      paymentServiceActor = paymentService
  }
}
