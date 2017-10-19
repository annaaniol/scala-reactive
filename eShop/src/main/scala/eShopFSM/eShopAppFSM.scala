package eShopFSM

import akka.actor.{ActorSystem, Props}

object eShopAppFSM extends App {
  val system = ActorSystem("eShopApp")

  val cart = system.actorOf(Props[CartFSM], "cartFSM")
  val checkout = system.actorOf(Props[CheckoutFSM], "checkoutFSM")

  cart ! ItemAdded("chomik")
  cart ! ItemAdded("papuga")
  cart ! ItemRemoved("chomik")
  cart ! ItemAdded("kanarek")
  cart ! ItemAdded("ryba")
  cart ! CheckoutStartedCart(checkout)

}
