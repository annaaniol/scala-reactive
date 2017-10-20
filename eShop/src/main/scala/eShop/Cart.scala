package eShop

import akka.actor.{Actor, Props, Timers}

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
      printCart()
      context.become(notEmpty)
    case msg =>
      println("Failed in empty. Unhandled message: " + msg)
  }

  def notEmpty: Receive = {
    case ItemRemoved(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter -= 1
      items -= item
      printCart()
      if(itemCounter==0)
      {
        context.become(empty)
      }
    case ItemAdded(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      itemCounter += 1
      items += item
      printCart()
    case CheckoutStarted() if itemCounter > 0 =>
      val checkoutActor = context.actorOf(Props[Checkout], "checkoutActor")
      sender ! checkoutActor
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
      print("Checkout cancelled. ")
      printCart()
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      context.become(notEmpty)
    case msg =>
      println("Failed in inCheckout. Unhandled message: " + msg)
  }

  def printCart() = {
    print("Cart state: ")
    items.foreach(i => print(i + " "))
    println()
  }
}
