package eShop

case class ItemToAdd(item: Item)
case class ItemToRemove(item: Item)
case class RemoveAll()

case class Item(name: String)

case class Cart(state: List[Item]) {
  def add(item: Item): Cart = copy(item :: state)
  def remove(item: Item): Cart = copy(state.filter(_ != item))
  def removeAll(): Cart = copy(List.empty)
  def print(): String = state.toString()
  def size(): Int = state.length
  override def toString: String = state.toString()
}

object Cart {
  val empty = Cart(List.empty)
}