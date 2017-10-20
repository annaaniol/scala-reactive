package eShop

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import eShop._

object eShopApp extends App {
  val system = ActorSystem("eShopApp")
  val cart = system.actorOf(Props[Cart], "cart")
  val checkout = system.actorOf(Props[Checkout], "checkout")

  cart ! ItemAdded("mysz")
  cart ! ItemAdded("koszatniczka")
  cart ! ItemRemoved("mysz")
  cart ! CheckoutStartedCart(checkout)

  Thread.sleep(100)

  checkout ! DeliverySelected()
  checkout ! PaymentSelected()

  Thread.sleep(11000)

  cart ! ItemAdded("kobra")

  Await.result(system.whenTerminated, Duration.Inf)
}
