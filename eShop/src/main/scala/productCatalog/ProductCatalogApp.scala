package productCatalog

import scala.concurrent.duration.Duration
import akka.actor.{ActorSystem, Props}
import eShop.Item
import akka.pattern.ask
import akka.util.Timeout
import eShop.eShopMessages._

import scala.concurrent.duration._
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

import scala.concurrent.Await

object ProductCatalogApp extends App {

  val productCatalogSystem = ActorSystem("productCatalogApp")
  val productCatalogManager = productCatalogSystem.actorOf(Props[ProductCatalogManager],
    "productCatalogManager")

  implicit val waitForResponse = Timeout(10 seconds)

  val itemsListFuture = productCatalogManager ? GetItems("Hot Paprika Tin")
  val itemsList = Await.result(itemsListFuture, waitForResponse.duration).asInstanceOf[List[Item]]
  println("Received response: " + itemsList.toString + "\n\n")

  Await.result(productCatalogSystem.whenTerminated, Duration.Inf)

}
