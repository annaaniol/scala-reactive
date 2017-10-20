package eShopFSM

import akka.actor.ActorRef

sealed trait eShopCartMessage
sealed trait eShopCheckoutMessage

case class ItemAdded(item: String) extends eShopCartMessage
case class ItemRemoved(item: String) extends eShopCartMessage
case class CheckoutStartedCart(remoteCheckout: ActorRef) extends eShopCartMessage
case class CartTimeout() extends eShopCartMessage

case class CheckoutStarted() extends eShopCheckoutMessage
case class CheckoutClosed() extends eShopCheckoutMessage
case class CheckoutCancelled() extends eShopCheckoutMessage
case class CheckoutTimeout() extends eShopCheckoutMessage
case class DeliveryMethodSelected() extends eShopCheckoutMessage
case class PaymentSelected() extends eShopCheckoutMessage
case class PaymentReceived() extends eShopCheckoutMessage
case class PaymentTimeout() extends eShopCheckoutMessage



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
