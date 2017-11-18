package eShop

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import eShop._

class CheckoutTest extends TestKit(ActorSystem("CheckoutTest"))
  with WordSpecLike with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart" should {

    "should get a response from Checkout when checkout is successfully closed" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-11"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! DeliverySelected()
      child ! PaymentSelected()
      child ! PaymentReceived()
      parent.expectMsg(CheckoutClosed())
    }

    "should get a response from Checkout when checkout is cancelled in selectingDelivery" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-12"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! CheckoutCancelled()
      parent.expectMsg(CheckoutCancelled())
    }

    "should get a response from Checkout when checkout is cancelled in selectingPaymentMethod" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-13"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! DeliverySelected()
      child ! CheckoutCancelled()
      parent.expectMsg(CheckoutCancelled())
    }

    "should get a response from Checkout when checkout is cancelled in processingPayment" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-14"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! DeliverySelected()
      child ! PaymentSelected()
      child ! CheckoutCancelled()
      parent.expectMsg(CheckoutCancelled())
    }

    "should get a response from Checkout during 10 seconds of inactivity in selectingDelivery" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-15"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      parent.expectMsg(10.seconds, CheckoutCancelled())
    }

    "should get a response from Checkout during 10 seconds of inactivity in selectingPaymentMethod" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-16"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! DeliverySelected()
      parent.expectMsg(10.seconds, CheckoutCancelled())
    }

    "should get a response from Checkout during 10 seconds of inactivity in processingPayment" in {
      val parent = TestProbe()
      val checkoutTestId = "checkout-test-id-17"
      val child = parent.childActorOf(Checkout.props(checkoutTestId))
      child ! DeliverySelected()
      parent.expectNoMsg()
      child ! PaymentSelected()
      parent.expectMsg(10.seconds, CheckoutCancelled())
    }
  }

  "A Checkout" should {

    "terminate when payment is received" in {
      val testProbe = TestProbe()
      val checkoutTestId = "checkout-test-id-18"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      testProbe watch checkout
      checkout ! DeliverySelected()
      checkout ! PaymentSelected()
      checkout ! PaymentReceived()
      testProbe.expectTerminated(checkout)
    }

    "terminate when checkout is cancelled" in {
      val testProbe = TestProbe()
      val checkoutTestId = "checkout-test-id-19"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      testProbe watch checkout
      checkout ! DeliverySelected()
      checkout ! PaymentSelected()
      checkout ! CheckoutCancelled()
      testProbe.expectTerminated(checkout)
    }

    "terminate during 10 seconds of inactivity" in {
      val testProbe = TestProbe()
      val checkoutTestId = "checkout-test-id-10"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      testProbe watch checkout
      testProbe.expectTerminated(checkout, 10.seconds)
    }

    "terminate during 10 seconds of inactivity after selecting delivery" in {
      val testProbe = TestProbe()
      val checkoutTestId = "checkout-test-id-11"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      testProbe watch checkout
      checkout ! DeliverySelected()
      testProbe.expectTerminated(checkout, 10.seconds)
    }

    "terminate during 10 seconds of inactivity after selecting payment" in {
      val testProbe = TestProbe()
      val checkoutTestId = "checkout-test-id-12"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      testProbe watch checkout
      checkout ! DeliverySelected()
      checkout ! PaymentSelected()
      testProbe.expectTerminated(checkout, 10.seconds)
    }
  }

  "A Checkout" should {
    "persist and recover its state" in {
      val cartParent = TestProbe()

      val checkoutTestId = "checkout-persistence-id-13"
      val checkout = cartParent.childActorOf(Checkout.props(checkoutTestId))

      checkout ! DeliverySelected()
      cartParent.expectNoMsg()
      checkout ! PaymentSelected()
      cartParent.expectNoMsg()
      checkout ! PoisonPill

      val checkout2 = cartParent.childActorOf(Checkout.props(checkoutTestId))

      checkout2 ! PaymentReceived()
      cartParent.expectMsg(CheckoutClosed())
    }
  }
}