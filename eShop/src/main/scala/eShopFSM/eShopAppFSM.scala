package eShopFSM

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await

object eShopAppFSM extends App {
  val system = ActorSystem("eShopApp")
  val cart = system.actorOf(Props[CartFSM], "cartFSM")
  implicit val waitForCheckoutRefTimeout = Timeout(100 milliseconds)
  var checkout :ActorRef = null

  cart ! ItemAdded("chomik")
  cart ! ItemAdded("papuga")
  cart ! ItemRemoved("chomik")
  cart ! ItemAdded("kanarek")
  cart ! ItemAdded("ryba")

  checkout = Await.result(cart ? CheckoutStarted(), waitForCheckoutRefTimeout.duration).asInstanceOf[ActorRef]

  Thread.sleep(100)

  checkout ! DeliveryMethodSelected()
  checkout ! PaymentSelected()
  checkout ! CheckoutCancelled()

  Thread.sleep(100)

  checkout = Await.result({cart ? CheckoutStarted()}, waitForCheckoutRefTimeout.duration).asInstanceOf[ActorRef]

  checkout ! DeliveryMethodSelected()
  checkout ! PaymentSelected()
  checkout ! PaymentReceived()

}
