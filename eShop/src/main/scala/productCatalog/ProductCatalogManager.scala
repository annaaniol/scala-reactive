package productCatalog

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import eShop.Item
import akka.pattern.ask

import scala.concurrent.duration._
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

import scala.concurrent.Await
import scala.util.matching.Regex

class ProductCatalogManager extends Actor
  with ActorLogging{

  val productCatalogRouter = context.actorOf(Props[ProductCatalogRouter])

  val host = "localhost"
  val port = 8555

  implicit val system = ActorSystem("simple-rest-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val waitForResponse = Timeout(10 - 1 seconds)
  val counter : AtomicInteger = new AtomicInteger()
  var catalogRequest: Regex = """"/\w+""".r

  val route: Route = {
    path("catalog" / """\w+""".r) {
      matched:String => {
        get {
          log.info("Processing catalog request: " + matched)

          val response = Await.result(productCatalogRouter ? GetItems(matched), 10 seconds).asInstanceOf[List[Item]]

          complete{
            HttpEntity(
              ContentTypes.`application/json`, response.toString
            )
          }
        }
      }
    }
  }

  val binding = Http().bindAndHandle(route, host, port)

  override def receive = {
    case GetItems(keyPhrase: String) =>
      productCatalogRouter forward GetItems(keyPhrase)

    case HowManyItems(name: String) =>
      productCatalogRouter forward HowManyItems(name)
  }
}
