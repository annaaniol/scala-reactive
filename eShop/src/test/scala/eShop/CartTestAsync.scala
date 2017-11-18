package eShop

import java.net.URI

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

  val chocolate = Item(new URI("www.chocolate.pl"), "chocolate", 5.00, 1)
  val tea = Item(new URI("www.tea.pl"), "tea", 40.00, 1)
  val coffee = Item(new URI("www.coffee.pl"), "coffee", 50.00, 1)

  "A Cart" must {

    "start checkout when is not empty" in {
      val cartTestId = "test-id-11"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(chocolate)
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
    }

    "not start checkout when is empty" in {
      val cartTestId = "test-id-12"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(chocolate)
      cart ! ItemToRemove(chocolate)
      cart ! StartCheckout()
      expectNoMsg()
    }

    "send 'Empty notification' after closing checkout successfully" in {
      val cartTestId = "test-id-13"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! ItemToAdd(chocolate)
      cart ! StartCheckout()
      expectMsgType[CheckoutStarted]
      cart ! CheckoutClosed()
      expectMsg(CartEmpty())
    }

    "preserve its state after restart" in {
      val cartTestId = "test-id-14"
      val cart = system.actorOf(CartManager.props(cartTestId))
      cart ! RemoveAll()

      cart ! ItemToAdd(chocolate)
      cart ! ItemToAdd(tea)

      cart ! ShowState()
      expectMsg(2)

      cart ! PoisonPill

      val cart2 = system.actorOf(CartManager.props(cartTestId))

      cart2 ! ShowState()
      expectMsg(2)

      cart2 ! ItemToRemove(chocolate)
      cart2 ! ItemToAdd(coffee)

      // In 5 seconds it exceeds defined time limit
      within(6 seconds) {
        expectMsg(CartEmpty())
      }

      cart2 ! CheckoutClosed()
    }
  }
}
