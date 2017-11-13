package eShop

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import eShop._

class CartTestSync extends TestKit(ActorSystem("CartTestSync"))
  with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart" must {

    "start with no items" in {
      val counter = TestActorRef[Cart]
      assert (counter.underlyingActor.items.isEmpty)
    }

    "increment the size of items set when item is added" in {
      val counter = TestActorRef[Cart]
      counter ! AddItem("testItem")
      assert (counter.underlyingActor.items.size == 1)
    }

    "adjust the size of items set when items are added and removed" in {
      val counter = TestActorRef[Cart]
      counter ! AddItem("itemA")
      counter ! AddItem("itemB")
      counter ! AddItem("itemC")
      counter ! ItemRemoved("itemA")
      assert (counter.underlyingActor.items.size == 2)
    }

    "manage selected items" in {
      val counter = TestActorRef[Cart]
      counter ! AddItem("itemA")
      counter ! AddItem("itemB")
      counter ! ItemRemoved("itemA")
      assert (!counter.underlyingActor.items.contains("itemA"))
      assert (counter.underlyingActor.items.contains("itemB"))
    }
  }
}
