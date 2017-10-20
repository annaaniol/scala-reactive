package eShopFSM

import akka.actor.{Actor, FSM, Props}

class CartFSM extends Actor with FSM[State, Data] {

  startWith(Empty, Uninitialized)

  when(Empty) {
    case Event(ItemAdded(item), Uninitialized) => {
      print("Cart state (added to empty): " + item)
      goto(NotEmpty) using Cart(Set(item))
    }
  }

  when(NotEmpty) {
    case Event(ItemAdded(item), Cart(items)) => {
      print("\nCart state (added to notEmpty): ")
      items.foreach(i => print(i + " "))
      print(item)
      stay using Cart(items + item)
    }
    case Event(ItemRemoved(item), Cart(items)) if items.size > 1 => {
      print("\nCart state (removed from notEmpty): ")
      items.foreach(i => if(!i.equals(item))print(i + " "))
      stay using Cart(items - item)
    }
    case Event(ItemRemoved(_), Cart(items)) if items.size == 1 => {
      println("Last item removed")
      goto(Empty)
    }
    case Event(CheckoutStarted(), Cart(items)) => {
      println("\nInitializing checkout (CartFSM)")
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
      items.foreach(i => print(i + " "))
      goto(NotEmpty) using Cart(items)
    }
  }
}
