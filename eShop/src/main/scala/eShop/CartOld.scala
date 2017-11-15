package eShop

import akka.actor.{Actor, ActorRef, Props, Timers}

import scala.concurrent.duration._
import scala.collection.mutable.Set
import eShop._

case class CartOld(items: Set[String]) extends Actor with Timers {

  var remoteCustomer :ActorRef = null

  //var items = Set[String]()

  def receive = empty

  def empty: Receive = {
    case AddItem(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      items += item
      remoteCustomer = sender
      printCart()
      context.become(notEmpty)
    case msg =>
      println("Failed in empty. Unhandled message: " + msg)
  }

  def notEmpty: Receive = {
    case ItemRemoved(item) if items.size > 1 && items.contains(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      items -= item
      printCart()
    case ItemRemoved(item) if items.size == 1 && items.contains(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      println("Last item removed")
      context.become(empty)
    case AddItem(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      items += item
      printCart()
    case StartCheckout() if items.nonEmpty =>
      val checkoutActor = context.actorOf(Props[Checkout], "checkoutActor")
      remoteCustomer ! CheckoutStarted(checkoutActor)
      timers.cancel(CartTimerKey)
      context.become(inCheckout)
    case CartTimeout() =>
      println("Cart Timeout in notEmpty. You are back in empty state")
      items.clear()
      remoteCustomer ! CartEmpty()
      context.become(empty)
    case msg =>
      println("Failed in notEmpty. Unhandled message: " + msg)
  }

  def inCheckout: Receive = {
    case CheckoutClosed() =>
      println("Checkout closed successfully. Congratulations!")
      remoteCustomer ! CartEmpty()
      items.foreach(el => items.remove(el))
      context.become(empty)
    case CheckoutCancelled() =>
      println("Checkout cancelled")
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
