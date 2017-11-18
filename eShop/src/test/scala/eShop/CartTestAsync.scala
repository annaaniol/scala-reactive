package eShop

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import eShop._

class CartTestAsync extends TestKit(ActorSystem("CartTestAsync"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A Cart" must {

    "start checkout when is not empty" in {
      val cartTestId = "test-id-01"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(Item("itemA"))
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
    }

    "not start checkout when is empty" in {
      val cartTestId = "test-id-02"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(Item("itemA"))
      cart ! ItemToRemove(Item("itemA"))
      cart ! StartCheckout()
      expectNoMsg()
    }

    "send 'Empty notification' after closing checkout successfully" in {
      val cartTestId = "test-id-03"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(Item("itemA"))
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
      cart ! CheckoutClosed()
      expectMsg(CartEmpty())
    }

    "preserve its state after restart" in {
      val cartTestId = "test-id-05"
      val cart = system.actorOf(CartManager.props(cartTestId))
      val item1 = Item("item1")
      val item2 = Item("item2")
      cart ! RemoveAll()

      cart ! ItemToAdd(item1)
      cart ! ItemToAdd(item2)

      cart ! ShowState()
      expectMsg(2)

      cart ! PoisonPill

      val cart2 = system.actorOf(CartManager.props(cartTestId))

      cart2 ! ShowState()
      expectMsg(2)

      cart2 ! ItemToRemove(item1)
      cart2 ! ItemToAdd(item1)

      // In 5 seconds it exceeds defined time limit
      within(6 seconds) {
        expectMsg(CartEmpty())
      }

      cart2 ! CheckoutClosed()
    }
  }
}
