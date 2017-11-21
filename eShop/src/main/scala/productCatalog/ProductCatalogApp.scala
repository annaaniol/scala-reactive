package productCatalog

import scala.concurrent.duration.Duration
import akka.actor.{ActorSystem, Props}
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

import scala.concurrent.Await

object ProductCatalogApp extends App {

  val productCatalogSystem = ActorSystem("productCatalogApp")
  val productCatalogManager = productCatalogSystem.actorOf(Props[ProductCatalogManager],
    "productCatalogManager")

  productCatalogManager ! GetItems("Hot Paprika Tin")

  Await.result(productCatalogSystem.whenTerminated, Duration.Inf)

}
