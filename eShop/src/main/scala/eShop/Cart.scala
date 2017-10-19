package eShop

import akka.actor.{Actor}
import akka.actor.Timers

import scala.concurrent.duration._

class Cart extends Actor with Timers {
  import eShop._

  var itemCounter = BigInt(0)
  var items = Set[String]()

  def receive = empty

  def empty: Receive = {
    case eShop.ItemAdded(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter += 1
      items += item
      print("\nCart state: ")
      items.foreach(i => print(i + " "))
      context.become(notEmpty)
    case _ =>
      println("Failed in empty state")
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
    case CheckoutStartedCart(checkout) =>
      if(itemCounter>0){
        checkout ! CheckoutStarted()
        timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 10.seconds)
        timers.cancel(CartTimerKey)
        context.become(inCheckout)
      } else {
        println("Error occurred. Processing checkout request but cart is empty")
      }
    case CartTimeout() =>
      itemCounter = 0
      items = items.empty
      println("Timeout in notEmpty! Cart is empty")
      context.become(empty)
    case _ =>
      println("Failed in notEmpty state")
  }

  def inCheckout: Receive = {
    case CheckoutClosed() =>
      timers.cancel(CheckoutTimerKey)
      println("Checkout closed successfully. Congratulations!")
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
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      context.become(notEmpty)
    case _ =>
      println("Failed in inCheckout state")
  }

}
