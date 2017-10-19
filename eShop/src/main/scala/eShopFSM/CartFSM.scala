package eShopFSM

import akka.actor.{Actor, FSM}

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
    case Event(ItemRemoved(item), Cart(items)) if items.size == 1 => {
      println("Last item removed")
      goto(Empty)
    }
    case Event(CheckoutStartedCart(checkoutActor), Cart(items)) =>
    {
      println("\nCheckout started")
      checkoutActor ! CheckoutStarted(items)
      goto(InCheckout) using Cart(items)
    }
    case Event(CartTimeout(), Uninitialized) => {
      println("Timeout in notEmpty! Cart becomes empty")
      goto(Empty)
    }
  }

  when(InCheckout) {
    case Event(CheckoutClosed(), Cart(items)) => {
      println("Checkout closed successfully. Congratulations!")
      goto(Empty)
    }
    case Event(CheckoutCancelled(), Cart(items)) => {
      println("Checkout canceled")
      goto(NotEmpty) using Cart(items)
    }
  }
}
