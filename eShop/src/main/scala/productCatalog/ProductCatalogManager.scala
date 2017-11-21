package productCatalog

import akka.actor.{Actor, ActorLogging, Props}
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

class ProductCatalogManager extends Actor
  with ActorLogging{

  val productCatalog = context.actorOf(Props[ProductCatalog])

  override def receive = {
    case GetItems(keyPhrase: String) =>
      productCatalog forward GetItems(keyPhrase)

    case HowManyItems(name: String) =>
      productCatalog forward HowManyItems(name)
  }
}
