package eShop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import eShop._
import eShopFSM.CheckoutStarted

object eShopApp extends App {
  val system = ActorSystem("eShopApp")
  val cart = system.actorOf(Props[Cart], "cart")
  implicit val waitForCheckoutRefTimeout = Timeout(100 milliseconds)
  var checkout :ActorRef = null

  cart ! ItemAdded("mysz")
  cart ! ItemAdded("koszatniczka")
  cart ! ItemRemoved("mysz")

  checkout = Await.result(cart ? CheckoutStarted(), waitForCheckoutRefTimeout.duration).asInstanceOf[ActorRef]

  Thread.sleep(100)

  checkout ! DeliverySelected()
  checkout ! PaymentSelected()

  Thread.sleep(11000)

  cart ! ItemAdded("kobra")

  Await.result(system.whenTerminated, Duration.Inf)
}
