package eShopFSM

import akka.actor.{Actor, FSM}

class CheckoutFSM extends Actor with FSM[State, Data] {

  startWith(SelectingDelivery, CartActor(context.parent))

  when(SelectingDelivery) {
    case Event(DeliveryMethodSelected(), CartActor(actorRef)) => {
      println("Delivery type selected")
      goto(SelectingPaymentMethod) using CartActor(actorRef)
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on SelectingDelivery")
      actorRef ! CheckoutCancelled()
      stay using CartActor(actorRef)
    }
    case Event(CheckoutTimeout(), CartActor(actorRef)) => {
      println("Timeout in SelectingDelivery")
      actorRef ! CheckoutCancelled()
      stop()
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
      stop()
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on SelectingPaymentMethod")
      actorRef ! CheckoutCancelled()
      stop()
    }
  }

  when(ProcessingPayment) {
    case Event(PaymentReceived(), CartActor(actorRef)) => {
      println("Payment received")
      actorRef ! CheckoutClosed()
      stop()
    }
    case Event(PaymentTimeout(), CartActor(actorRef)) => {
      println("Timeout on ProcessingPayment")
      actorRef ! CheckoutCancelled()
      stop()
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on ProcessingPayment")
      actorRef ! CheckoutCancelled()
      stop()
    }
  }
}
