package eShopFSM

import akka.actor.{Actor, FSM, Props}

class CartFSM extends Actor with FSM[State, Data] {

  startWith(Empty, Uninitialized)

  when(Empty) {
    case Event(ItemAdded(item), Uninitialized) => {
      println("Cart state: " + item)
      goto(NotEmpty) using Cart(Set(item))
    }
  }

  when(NotEmpty) {
    case Event(ItemAdded(item), Cart(items)) => {
      printCart(items + item)
      stay using Cart(items + item)
    }
    case Event(ItemRemoved(item), Cart(items)) if items.size > 1 && items.contains(item) => {
      printCart(items - item)
      stay using Cart(items - item)
    }
    case Event(ItemRemoved(item), Cart(items)) if items.size == 1 && items.contains(item) => {
      println("Last item removed")
      goto(Empty)
    }
    case Event(CheckoutStarted(), Cart(items)) => {
      println("Initializing checkout")
      val checkoutActor = context.actorOf(Props[CheckoutFSM], "checkoutActor")
      sender ! checkoutActor
      goto(InCheckout) using Cart(items)
    }
    case Event(CartTimeout(), Uninitialized) => {
      println("Timeout in notEmpty! Cart becomes empty")
      goto(Empty)
    }
  }

  when(InCheckout) {
    case Event(CheckoutClosed(), Cart(items)) => {
      print("Checkout closed successfully. You purchased the following items: ")
      items.foreach(i => print(i + " "))
      goto(Empty)
    }
    case Event(CheckoutCancelled(), Cart(items)) => {
      println("Checkout cancelled")
      printCart(items)
      goto(NotEmpty) using Cart(items)
    }
  }

  def printCart(items: Set[String]) = {
    print("Cart state: ")
    items.foreach(i => print(i + " "))
    println()
  }
}
