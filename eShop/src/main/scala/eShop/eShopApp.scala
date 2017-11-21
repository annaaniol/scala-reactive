package eShop

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.pattern.ask

import scala.concurrent.duration._
import productCatalog.ProductCatalogMessages.GetItems
import productCatalog._

import scala.concurrent.Await

object eShopApp extends App {

  val config = ConfigFactory.load()

  val eShopSystem = ActorSystem("eShop", config.getConfig("eShopSystem").withFallback(config))
  val productCatalogSystem = ActorSystem("productCatalog", config.getConfig("productCatalogSystem").withFallback(config))

  // This App simulates Customer Actor
  val cartId = "app-cart-id-01"
  val cartManager = eShopSystem.actorOf(CartManager.props(cartId))
  val productCatalogManager = productCatalogSystem.actorOf(Props[ProductCatalogManager])
  implicit val waitForResponseFromCatalog = Timeout(10 seconds)

  val ItemsListFuture = productCatalogManager ? GetItems("Hot Paprika Tin")
  val ItemsList = Await.result(ItemsListFuture, waitForResponseFromCatalog.duration).asInstanceOf[List[Item]]
  println("Received response: " + ItemsList.toString)
}
