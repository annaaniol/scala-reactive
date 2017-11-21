package eShop

import akka.actor.{ActorLogging, ActorRef, Props, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import akka.persistence._
import eShopMessages._


class CartManager(id: String) extends PersistentActor
  with Timers with ActorLogging {

  override def persistenceId = id
  var checkoutPersistenceId = "checkout-id-01"

  var remoteCustomer :ActorRef = _

  var cartState = Cart(Map())

  def updateStateAfterAdding(item: Item): Unit = {
    log.info("In update after adding " + item.name)
    cartState = cartState.addItem(item)
    saveSnapshot(cartState)
  }

  def updateStateAfterRemoving(item: Item): Unit = {
    log.info("In update after removing " + item.name)
    cartState = cartState.removeItem(item)
    saveSnapshot(cartState)
  }

  def updateStateAfterRemovingAll(): Unit = {
    log.info("In update after removing all")
    cartState = cartState.removeAll()
    saveSnapshot(cartState)
  }

  def numItems : Int = cartState.size

  def printCart() = {
    log.info("Cart size: " + numItems + " Items in cart: " + cartState.print())
  }

  def receiveCommand : Receive = LoggingReceive {
    case ItemToAdd(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      persist(item)(updateStateAfterAdding)
      remoteCustomer = sender
      printCart()
    case ItemToRemove(item) =>
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      persist(item)(updateStateAfterRemoving)
      printCart()
    case RemoveAll() =>
      log.info("Removing all items")
      cartState = cartState.removeAll()
      printCart()
    case StartCheckout() if cartState.size > 0 =>
      val checkoutActor = context.actorOf(Checkout.props(checkoutPersistenceId), "checkoutActor")
      remoteCustomer ! CheckoutStarted(checkoutActor)
      timers.cancel(CartTimerKey)
    case StartCheckout() if cartState.size <= 0 =>
      log.info("Received StartCheckout message but the cart is empty!")
    case CartTimeout() =>
      updateStateAfterRemovingAll()
      remoteCustomer ! CartEmpty()
      log.info("Cart Timeout. All items removed from cart")
      printCart()
    case CheckoutClosed() =>
      updateStateAfterRemovingAll()
      remoteCustomer ! CartEmpty()
      log.info("Checkout closed successfully. Congratulations!")
      printCart()
    case CheckoutCancelled() =>
      log.info("Checkout cancelled")
      timers.startSingleTimer(CartTimerKey, CartTimeout(), 5.seconds)
      printCart()
    case ShowState() =>
      sender ! numItems
    case SaveSnapshotSuccess(_) =>
      log.info("Snapshot success")
    case msg =>
      log.info("Unexpected message: " + msg)
  }

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Cart) =>
      cartState = snapshot
  }
}

object CartManager {
  def props(id: String): Props = Props(new CartManager(id))
}
