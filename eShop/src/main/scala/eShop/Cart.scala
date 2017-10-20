package eShop

import akka.actor.Actor
import akka.actor.Timers

import scala.concurrent.duration._
import scala.collection.mutable.Set
import eShop._

class Cart extends Actor with Timers {

  var itemCounter = BigInt(0)
  var items = Set[String]()

  def receive = empty

  def empty: Receive = {
    case eShop.ItemAdded(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter += 1
      items += item
      println("Cart state: " + item)
      context.become(notEmpty)
    case msg =>
      println("Failed in empty. Unhandled message: " + msg)
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
    case CheckoutStartedCart(checkout) if itemCounter > 0 =>
      checkout ! CheckoutStarted()
      timers.cancel(CartTimerKey)
      context.become(inCheckout)
    case CartTimeout() =>
      println("Cart Timeout in notEmpty. You are back in empty state")
      items.clear()
      context.become(empty)
    case msg =>
      println("Failed in notEmpty. Unhandled message: " + msg)
  }

  def inCheckout: Receive = {
    case CheckoutClosed() =>
      println("Checkout closed successfully. Congratulations!")
      itemCounter = 0
      items = items.empty
      context.become(empty)
    case CheckoutCancelled() =>
      print("\nCheckout cancelled. You are back in a cart with: ")
      items.foreach(i => print(i + " "))
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      context.become(notEmpty)
    case msg =>
      println("Failed in inCheckout. Unhandled message: " + msg)
  }

}
