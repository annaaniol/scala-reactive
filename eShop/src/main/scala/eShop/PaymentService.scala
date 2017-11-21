package eShop

import akka.actor.{Actor, ActorRef, Timers}
import eShopMessages._

class PaymentService(remoteCustomer: ActorRef) extends Actor with Timers {

  def receive = awaitingPayment

  var remoteCheckout :ActorRef = context.parent

  def awaitingPayment: Receive = {
    case eShopMessages.DoPayment() =>
      println("PaymentService: Payment done")
      remoteCustomer ! PaymentConfirmed()
      remoteCheckout ! PaymentReceived()
      context.stop(self)
  }
}
