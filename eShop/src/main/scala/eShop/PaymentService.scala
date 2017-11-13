package eShop

import akka.actor.{Actor, ActorRef, Timers}
import eShop._

class PaymentService(remoteCustomer: ActorRef) extends Actor with Timers {

  def receive = awaitingPayment

  var remoteCheckout :ActorRef = context.parent

  def awaitingPayment: Receive = {
    case eShop.DoPayment() =>
      println("PaymentService: Payment done")
      remoteCustomer ! PaymentConfirmed()
      remoteCheckout ! PaymentReceived()
      context.stop(self)
  }
}
