package eShopFSM

import akka.actor.{Actor, FSM, Timers}

import scala.concurrent.duration._

class CheckoutFSM extends Actor with FSM[State, Data] with Timers {

  startWith(SelectingDelivery, CartActor(context.parent))

  timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)

  when(SelectingDelivery) {
    case Event(DeliveryMethodSelected(), CartActor(actorRef)) => {
      println("Delivery type selected")
      goto(SelectingPaymentMethod) using CartActor(actorRef)
    }
    case Event(CheckoutCancelled(), CartActor(actorRef)) => {
      println("Checkout cancelled on SelectingDelivery")
      actorRef ! CheckoutCancelled()
      stop()
    }
    case Event(CheckoutTimeout(), CartActor(actorRef)) => {
      println("Timeout in SelectingDelivery")
      actorRef ! CheckoutCancelled()
      stop()
    }
  }

  when(SelectingPaymentMethod) {
    case Event(PaymentSelected(), CartActor(actorRef)) => {
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
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
      timers.cancel(PaymentTimerKey)
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
