package eShopFSM

import akka.actor.ActorRef

sealed trait eShopMessage

case class ItemAdded(item: String) extends eShopMessage
case class ItemRemoved(item: String) extends eShopMessage
case class CartTimeout() extends eShopMessage
case class PassCheckoutActor(checkoutActor: ActorRef)

case class CheckoutStarted() extends eShopMessage
case class CheckoutClosed() extends eShopMessage
case class CheckoutCancelled() extends eShopMessage
case class CheckoutTimeout() extends eShopMessage
case class DeliveryMethodSelected() extends eShopMessage
case class PaymentSelected() extends eShopMessage
case class PaymentReceived() extends eShopMessage
case class PaymentTimeout() extends eShopMessage


sealed trait State
// States of CartFSM
case object Empty extends State
case object NotEmpty extends State
case object InCheckout extends State
// States of CheckoutFSM
case object Awaiting extends State
case object SelectingDelivery extends State
case object SelectingPaymentMethod extends State
case object ProcessingPayment extends State

sealed trait Data
case object Uninitialized extends Data
case class Cart(items: Set[String]) extends Data
case class CartWithActor(items: Set[String], cartActor: ActorRef) extends Data
case class CartActor(actorRef: ActorRef) extends Data
