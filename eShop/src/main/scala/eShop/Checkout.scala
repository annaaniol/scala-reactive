package eShop

import akka.actor.{ActorLogging, ActorRef, Props, Timers}
import akka.persistence.{PersistentActor, SaveSnapshotSuccess, SnapshotOffer}

import scala.concurrent.duration._
import eShop._

case class CheckoutState(stateName: String)

class Checkout(id: String) extends PersistentActor
  with Timers with ActorLogging {

  override def persistenceId = id
  def receiveCommand: Receive = selectingDelivery
  var checkoutState: String = _

  var remoteCustomer :ActorRef = _
  var remoteCart :ActorRef = context.parent

  timers.startSingleTimer(CheckoutTimerKey, CheckoutTimeout(), 5.seconds)

  def changeState(stateName: String): Unit = {
    log.info("In changeState !!!!\n\n")
    checkoutState = stateName
    saveSnapshot(checkoutState)
  }

  def selectingDelivery: Receive = {
    case CheckoutCancelled() =>
      log.info("Checkout cancelled")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case DeliverySelected() =>
      remoteCustomer = sender
      log.info("Delivery type selected")
      persist("selectingPaymentMethod")(changeState)
      context.become(selectingPaymentMethod)
    case CheckoutTimeout() =>
      log.info("Timeout in selectingDelivery! Checkout cancelled")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case SaveSnapshotSuccess(_) =>
      log.info("Snapshot success")
    case msg =>
      log.warning("Failed in selectingDelivery. Unhandled message: " + msg)
  }

  def selectingPaymentMethod: Receive = {
    case PaymentSelected() =>
      timers.cancel(CheckoutTimerKey)
      timers.startSingleTimer(PaymentTimerKey, PaymentTimeout(), 5.seconds)
      val paymentServiceActor = context.actorOf(Props(new PaymentService(remoteCustomer)), "paymentServiceActor")
      remoteCustomer ! PaymentServiceStarted(paymentServiceActor)
      log.info("Payment method selected")
      persist("processingPayment")(changeState)
      context.become(processingPayment)
    case CheckoutCancelled() =>
      log.info("Checkout cancelled on selectingPaymentMethod")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case CheckoutTimeout() =>
      log.info("Timeout in selectingPaymentMethod")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case SaveSnapshotSuccess(_) =>
      log.info("Snapshot success")
    case msg =>
      log.warning("Failed in selectingPaymentMethod. Unhandled message: " + msg)
  }

  def processingPayment: Receive = {
    case PaymentReceived() =>
      timers.cancel(PaymentTimerKey)
      log.info("Payment received.")
      remoteCart ! CheckoutClosed()
      context.stop(self)
    case PaymentTimeout() =>
      log.info("Timeout in processingPayment")
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case CheckoutCancelled() =>
      log.info("Checkout cancelled on processingPayment")
      timers.cancelAll()
      remoteCart ! CheckoutCancelled()
      context.stop(self)
    case SaveSnapshotSuccess(_) =>
      log.info("Snapshot success")
    case msg =>
      log.warning("Failed in processingPayment. Unhandled message: " + msg)
  }

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: String) => {
      checkoutState = snapshot
      log.info("Recovered state: " + checkoutState)
      if (checkoutState == "selectingPaymentMethod") context become selectingPaymentMethod
      else if (checkoutState == "processingPayment") context become processingPayment
      else context become selectingDelivery
    }
  }
}

object Checkout {
  def props(id: String): Props = Props(new Checkout(id))
}
