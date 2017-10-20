package eShopFSM

import akka.actor.{Actor, FSM}

class CheckoutFSM extends Actor with FSM[State, Data] {

  startWith(Awaiting, Uninitialized)

  when(Awaiting) {
    case Event(CheckoutStarted(), _) => {
      println("Checkout started (CheckoutFSM)")
      goto(SelectingDelivery) using CartActor(sender)
    }
  }

  when(SelectingDelivery) {
    case Event(DeliveryMethodSelected(), CartActor(actorRef)) => {
      println("Delivery type selected")
      goto(SelectingPaymentMethod) using CartActor(actorRef)
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled")
      actorRef ! CheckoutCancelled()
      stay using CartActor(actorRef)
    }
    case Event(CheckoutTimeout(), CartActor(actorRef)) => {
      println("Timeout in SelectingDelivery")
      actorRef ! CheckoutCancelled()
      goto(Awaiting)
    }
  }

  when(SelectingPaymentMethod) {
    case Event(PaymentSelected(), CartActor(actorRef)) => {
      println("Payment method selected")
      goto(ProcessingPayment) using CartActor(actorRef)
    }
    case Event(CheckoutTimeout(), CartActor(actorRef)) => {
      println("Timeout in SelectingPaymentMethod")
      actorRef ! CheckoutCancelled()
      goto(Awaiting)
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on SelectingPaymentMethod")
      actorRef ! CheckoutCancelled()
      goto(Awaiting)
    }
  }

  when(ProcessingPayment) {
    case Event(PaymentReceived(), CartActor(actorRef)) => {
      actorRef ! CheckoutClosed()
      goto(Awaiting)
    }
    case Event(PaymentTimeout(), CartActor(actorRef)) => {
      println("Timeout on ProcessingPayment")
      actorRef ! CheckoutCancelled()
      goto(Awaiting)
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on ProcessingPayment")
      actorRef ! CheckoutCancelled()
      goto(Awaiting)
    }
  }
}
