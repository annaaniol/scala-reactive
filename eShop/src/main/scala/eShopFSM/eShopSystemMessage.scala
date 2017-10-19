package eShopFSM

import akka.actor.ActorRef

sealed trait eShopCartMessage
sealed trait eShopCheckoutMessage

case class ItemAdded(item: String) extends eShopCartMessage
case class ItemRemoved(item: String) extends eShopCartMessage
case class CheckoutStartedCart(remoteCheckout: ActorRef) extends eShopCartMessage
case class CartTimeout() extends eShopCartMessage

case class CheckoutStarted(items: Set[String]) extends eShopCheckoutMessage
case class CheckoutClosed() extends eShopCheckoutMessage
case class CheckoutCancelled() extends eShopCheckoutMessage
case class CheckoutTimeout() extends eShopCheckoutMessage

sealed trait State
case object Empty extends State
case object NotEmpty extends State
case object InCheckout extends State
case object Awaiting extends State


sealed trait Data
case object Uninitialized extends Data
case class Cart(items: Set[String]) extends Data
