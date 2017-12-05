package eShop

import java.net.URI

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import eShop.eShopMessages._

import scala.concurrent.duration._
import productCatalog.ProductCatalogMessages.GetItems
import productCatalog._

import scala.concurrent.Await

object eShopApp extends App {

  val config = ConfigFactory.load()

  val eShopSystem = ActorSystem("eShop", config.getConfig("eShopSystem").withFallback(config))
  val productCatalogSystem = ActorSystem("productCatalog", config.getConfig("productCatalogSystem").withFallback(config))
  val paymentSystem = ActorSystem("payment", config.getConfig("paymentSystem").withFallback(config))

  // This App simulates Customer Actor
  val cartId = "app-cart-id-06"
  val checkoutId = "app-checkout-id-06"

  val cartManager = eShopSystem.actorOf(CartManager.props(cartId))
  //val checkout = eShopSystem.actorOf(Checkout.props(checkoutId))
  val productCatalogManager = productCatalogSystem.actorOf(Props[ProductCatalogManager])
  //val paymentService = paymentSystem.actorOf(Props[PaymentService])

//  implicit val waitForResponse = Timeout(10 seconds)

  /*
  Managed by rest api requests

  val itemsListFuture = productCatalogManager ? GetItems("Hot Paprika Tin")
  val itemsList = Await.result(itemsListFuture, waitForResponse.duration).asInstanceOf[List[Item]]

  println("Received response: " + itemsList.toString + "\n\n")
  */

 // val item = itemsList(0)
//
//  val item = new Item(URI.create("123"), "Nutella", 1, 2)
//  cartManager ! ItemToAdd(item)
//  cartManager ! ShowState()
//
//  val checkoutStartedFuture = cartManager ? StartCheckout()
//  val checkoutStarted = Await.result(checkoutStartedFuture, waitForResponse.duration).asInstanceOf[CheckoutStarted]
//  val checkout = checkoutStarted.checkout
//
//  checkout ! DeliverySelected()
//  val paymentServiceFuture = checkout ? PaymentSelected()
//  val paymentSelected = Await.result(paymentServiceFuture, waitForResponse.duration).asInstanceOf[PaymentServiceStarted]
//
//  val paymentService = paymentSelected.paymentService
//  paymentService ! DoPayment()

}
