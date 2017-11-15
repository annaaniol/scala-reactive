package eShop

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CartMain extends Actor {

  val cartManager = context.actorOf(Props[CartManager], "cart")

  var item1 = Item("first")
  var item2 = Item("second")

  println("wysylam add item do cart manager: " + item1.name)

  cartManager ! RemoveAll()
  cartManager ! ItemToAdd(item1)
  cartManager ! ItemToAdd(item2)
  cartManager ! ItemToRemove(item2)


  def receive = LoggingReceive {

    case "Done" =>
      context.system.terminate()

    case msg: String =>
      println(s" received: $msg")

  }
}

object PersistentCartApp extends App {
  val system = ActorSystem("PersistentSystem")
  val mainActor = system.actorOf(Props[CartMain], "cartMain")

  Await.result(system.whenTerminated, Duration.Inf)
}