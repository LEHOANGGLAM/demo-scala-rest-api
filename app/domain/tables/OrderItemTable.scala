package domain.tables


import domain.models.OrderItem
import slick.jdbc.PostgresProfile.api._
import java.time.LocalDateTime

class OrderItemTable(tag: Tag) extends Table[OrderItem](tag, Some("scalademo"), "order_items") {

  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  /** The orderId column */
  def orderId = column[Long]("orderid")

  /** The productId column */
  def productId = column[Long]("productid")

  /** The quantity column */
  def quantity = column[Int]("quantity")

  /** The totalPrice column */
  def price = column[Double]("price")

  /**
   * This is the table's default "projection".
   * It defines how the columns are converted to and from the User object.
   * In this case, we are simply passing the id, name, email and password parameters to the User case classes
   * apply and unapply methods.
   */
  def * =
    (id, orderId, productId,price, quantity) <> ((OrderItem.apply _).tupled, OrderItem.unapply)
}