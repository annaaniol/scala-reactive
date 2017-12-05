package productCatalog

import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import productCatalog.Item
import akka.pattern.ask
import play.api.libs.json._

import scala.concurrent.duration._
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

import scala.concurrent.Await
import scala.util.matching.Regex

case class Item(id: URI, name: String, price: BigDecimal, count: Int)

class ProductCatalogManager extends Actor
  with ActorLogging{

  val productCatalogRouter = context.actorOf(Props[ProductCatalogRouter])

  val host = "localhost"
  val port = 8555

  implicit val system = ActorSystem("simple-rest-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val waitForResponse = Timeout(10 - 1 seconds)

  implicit val uriReads = Reads{ js => js match {
    case JsString(s) => JsSuccess(java.net.URI.create(s))
    case _ => JsError("JsString expected to convert to URI")
  } }
  implicit val uriWrites = Writes{ uri: java.net.URI => JsString(uri.toString) }

  implicit val itemFormat: OFormat[Item] = Json.format[Item]

  val counter : AtomicInteger = new AtomicInteger()
  var catalogRequest: Regex = """"/\w+""".r

  val route: Route = {
    path("catalog" / """\w+""".r) {
      matched:String => {
        get {
          log.info("Processing catalog request: " + matched)

          val itemList = Await.result(productCatalogRouter ? GetItems(matched), 10 seconds).asInstanceOf[List[Item]]
          val items = Json.obj("items" -> itemList)

          complete{
            HttpEntity(
              ContentTypes.`application/json`, items.toString
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
