package productCatalog

import productCatalog.Item

object ProductCatalogMessages {

  case class GetItems(keyPhrase: String) {

  }

  case class HowManyItems(name: String) {

  }

}