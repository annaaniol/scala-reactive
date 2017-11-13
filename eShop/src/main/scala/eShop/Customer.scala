package eShop

import akka.actor.{Actor, ActorRef, Props, Timers}
import eShop._

class Customer extends Actor with Timers {

  var cartActor :ActorRef = null
  var checkoutActor :ActorRef = null
  var paymentServiceActor :ActorRef = null

  def receive = {
    case Start() =>
      cartActor = context.actorOf(Props[Cart], "cartActor")
    case CartEmpty() =>
      println("Customer: Cart is empty")
    case CheckoutStarted(checkout) =>
      checkoutActor = checkout
    case PaymentServiceStarted(paymentService) =>
      paymentServiceActor = paymentService
  }
}
