package eShop

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import eShop._

class CartTestAsync extends TestKit(ActorSystem("CartTestAsync"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart" must {

    "start checkout when is not empty" in {
      val cart = system.actorOf(Props[Cart])
      cart ! AddItem("itemA")
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
    }

    "not start checkout when is empty" in {
      val cart = system.actorOf(Props[Cart])
      cart ! AddItem("itemA")
      cart ! ItemRemoved("itemA")
      cart ! StartCheckout()
      expectNoMsg()
    }

    "send 'Empty notification' after closing checkout successfully" in {
      val cart = system.actorOf(Props[Cart])
      cart ! AddItem("itemA")
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
      cart ! CheckoutClosed()
      expectMsg(CartEmpty())
    }
  }
}
