package productCatalog

import eShop.Item

object ProductCatalogMessages {

  case class GetItems(keyPhrase: String) {

  }

  case class HowManyItems(name: String) {

  }

}