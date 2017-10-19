package eShop

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object eShopApp extends App {
  val system = ActorSystem("eShopApp")
  val cart = system.actorOf(Props[Cart], "cart")
  val checkout = system.actorOf(Props[Checkout], "checkout")

  cart ! eShop.ItemAdded("mysz")
  cart ! eShop.ItemAdded("koszatniczka")
  cart ! eShop.ItemRemoved("mysz")
  cart ! eShop.CheckoutStartedCart(checkout)

  Thread.sleep(100)

  checkout ! eShop.DeliverySelected()
  checkout ! eShop.PaymentSelected()
  checkout ! eShop.PaymentReceived()

  Await.result(system.whenTerminated, Duration.Inf)
}
