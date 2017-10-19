package eShopFSM

import akka.actor.{Actor, FSM}

class CheckoutFSM extends Actor with FSM[State, Data] {

  startWith(Awaiting, Uninitialized)

  when(Awaiting) {
    case Event(CheckoutStarted(items), Uninitialized) => {
      print("Checkout started")
      sender ! CheckoutClosed()
      stay using Cart(items)
    }
  }
}


