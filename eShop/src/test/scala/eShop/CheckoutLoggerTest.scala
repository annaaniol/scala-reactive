package eShop

import akka.actor.{ActorSystem, Props}
import akka.testkit.{EventFilter, TestKit}
import com.typesafe.config.ConfigFactory
import eShop._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CheckoutLoggerTest extends TestKit(ActorSystem("CheckoutLoggerTest", ConfigFactory.parseString("""
  akka.loggers = ["akka.testkit.TestEventListener"]
  """))) with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Checkout" should {

    "log all of the steps when checkout path successful" in {
      val checkoutTestId = "checkout-logger-id-01"
      val checkout = system.actorOf(Checkout.props(checkoutTestId))
      EventFilter.info(message="Delivery type selected", occurrences = 1) intercept {
        checkout ! DeliverySelected()
      }
      EventFilter.info(message="Payment method selected", occurrences = 1) intercept {
        checkout ! PaymentSelected()
      }
      EventFilter.info(message="Payment received", occurrences = 1) intercept {
        checkout ! PaymentReceived()
      }
    }
  }
}
