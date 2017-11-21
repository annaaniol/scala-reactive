package productCatalog

import scala.concurrent.duration.Duration
import akka.actor.{ActorSystem, Props}
import productCatalog.ProductCatalogMessages.GetItems

import scala.concurrent.Await

object ProductCatalogApp extends App {

  val productCatalogSystem = ActorSystem("productCatalogApp")
  val productCatalogManager = productCatalogSystem.actorOf(Props[ProductCatalogManager],
    "productCatalogManager")

  productCatalogManager ! GetItems("Toaster Muffins Corn")

  Await.result(productCatalogSystem.whenTerminated, Duration.Inf)

}
