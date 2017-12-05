package eShop

import java.net.URI

import productCatalog.Item

case class ItemToAdd(item: Item)
case class ItemToRemove(item: Item)
case class RemoveAll()

case class Cart(items: Map[URI, Item]) {
  def addItem(it: Item): Cart = {
    val currentCount = if (items contains it.id) items(it.id).count else 0
    copy(items = items.updated(it.id, it.copy(count = currentCount + it.count)))
  }

  def removeItem(item: Item): Cart = copy(items.filterKeys(_ != item.id))

  def removeAll(): Cart = copy(Map.empty)

  def print(): String = items.toString()

  def size(): Int = items.size

  override def toString: String = items.toString()
}

object Cart {
  val empty = Cart(Map.empty)
}