package eShop

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Timers

import scala.concurrent.Await
import scala.concurrent.duration._

object Cart {
  case class ItemAdded(item: String) {

  }
  case class ItemRemoved(item: String) {

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
  private case object CheckoutTimerKey
  private case object PaymentTimeout
  private case object CartTimerKey

  case object Done
  case object Failed
  case object Init

}

class Cart extends Actor with Timers {
  import Cart._

  var itemCounter = BigInt(0)
  var items = Set[String]()

  def receive = empty

  def empty: Receive = {
    case ItemAdded(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter += 1
      items += item
      print("\nCart state: ")
      items.foreach(i => print(i + " "))
      context.become(notEmpty)
    case _ =>
      println("Failed in empty state")
      sender ! Failed
  }

  def notEmpty: Receive = {
    case ItemRemoved(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter -= 1
      items -= item
      print("\nCart state: ")
      items.foreach(i => print(i + " "))
      if(itemCounter==0)
      {
        context.become(empty)
      }
    case ItemAdded(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter += 1
      items += item
      print("\nCart state: ")
      items.foreach(i => print(i + " "))
    case CheckoutStarted() =>
      timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)
      timers.cancel(CartTimerKey)
      context.become(inCheckout)
    case CartTimeout() =>
      itemCounter = 0
      items = items.empty
      println("Timeout in notEmpty! Cart is empty")
      context.become(empty)
    case _ =>
      println("Failed in notEmpty state")
      sender ! Failed
  }

  def inCheckout: Receive = {
    case CheckoutClosed() =>
      timers.cancel(CheckoutTimerKey)
      println("Checkout closed successfully")
      itemCounter = 0
      items = items.empty
      context.become(empty)
    case CheckoutCancelled() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      println("Checkout cancelled")
      context.become(notEmpty)
    case CheckoutTimeout() =>
      println("Timeout in inCheckout! Checkout canceled")
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      context.become(notEmpty)
    case _ =>
      println("Failed in inCheckout state")
      sender ! Failed
  }

}

object CartApp extends App {
  val system = ActorSystem("eShop")
  val cart = system.actorOf(Props[Cart], "cart")

  cart ! Cart.ItemAdded("mysz")
  cart ! Cart.ItemAdded("koszatniczka")
  cart ! Cart.ItemRemoved("mysz")
  cart ! Cart.CheckoutStarted()
  cart ! Cart.CheckoutClosed()


  Await.result(system.whenTerminated, Duration.Inf)
}
