package productCatalog

import java.net.URI

import akka.actor.{Actor, ActorLogging}
import eShop.Item
import productCatalog.ProductCatalogMessages.{GetItems, HowManyItems}

import scala.collection.mutable
import scala.io.BufferedSource

class ProductCatalog extends Actor
  with ActorLogging {
  log.info("ProductCatalog has started!")

  var items: Map[URI, Item] = Map.empty

  val bufferedSource: BufferedSource = scala.io.Source.fromFile("src/main/resources/productCatalog/query_result.csv")
  val lines: Iterator[String] = bufferedSource.getLines.drop(1)

  lines.map(
    l => l.split(",").map(_.replace("\"", ""))
  ).filter(
    l => l.size > 2
  ).foreach(
    l => {
      val uri = URI.create(l(0))
      val name = l(1)
      val brand = l(2)

      val random = new scala.util.Random
      val price = random.nextInt(100)
      val count = random.nextInt(1000)

      items += uri -> Item(uri, name + " " + brand, price, count)
    }
  )

  bufferedSource.close

  override def receive = {
    case GetItems(keyPhrase) => {
      log.info("ProductCatalog is executing GetItems()")
      val itemsURIList = findMatches(keyPhrase, 10)
      log.info("Top 10 items:\n"
        + itemsURIList.map(i => {
          i._1 + " occurrence(s) - " + items(i._2).name + "\n"
        })
      )

      val itemsList = itemsURIList.map(el => items(el._2))
      sender ! itemsList
    }

    case HowManyItems(name) => {
      sender ! items.map(i => {
        if (i._2.name.equals(name)) i._2.count else 0
      }).sum
    }
  }

  def findMatches(keyWord: String, topSize: Int): List[(Int, URI)] = {
    val topQueue: mutable.PriorityQueue[(Int, URI)] = mutable.PriorityQueue.empty[(Int, URI)].reverse

    items.foreach(
      i => {
        val occurrences = countOccurrences(i._2.name, keyWord)

        if (occurrences > 0) {
          if (topQueue.size < topSize) {
            topQueue.enqueue((occurrences, i._1))
            log.info(occurrences + " occurrences. Size below topSize. Enqueued " + i._2.name)
          } else if (occurrences > topQueue.min._1) {
            log.info(occurrences + " occurrences. Enqueued " + i._2.name)
            topQueue.dequeue()
            topQueue.enqueue((occurrences, i._1))
          }
        }
      }
    )

    val result: List[(Int, URI)] = topQueue.map(
      i => {
        (i._1, i._2)
      }
    ).toList

    result
  }

  def countOccurrences(baseString: String, keyPhrase: String): Int = {
    val splittedBase = baseString.split(" ")
    val splittedKeyPhrase = keyPhrase.split(" ")
    splittedBase.map(s => {
      splittedKeyPhrase.map(
        keyWord => {
          if(s.equals(keyWord)) 1 else 0
        }
      ).sum
    }).sum

  }
}
