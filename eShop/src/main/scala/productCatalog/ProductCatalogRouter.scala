package productCatalog

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}


class ProductCatalogRouter extends Actor
  with ActorLogging {

  val counter : AtomicInteger = new AtomicInteger()

  var router = {
    val routees = Vector.fill(2) {
      val catalogCount = counter.getAndIncrement()
      val r = context.actorOf(Props[ProductCatalog], name = s"product-catalog-$catalogCount")
      context watch r
      ActorRefRoutee(r)
    }
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  override def receive = {
    case GetItems(keyPhrase: String) =>
      router.route(GetItems(keyPhrase), sender())

    case HowManyItems(name: String) =>
      router.route(HowManyItems(name), sender())
  }
}
