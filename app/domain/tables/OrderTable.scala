package domain.tables


import domain.models.{Order}
import slick.jdbc.PostgresProfile.api._
import java.time.LocalDateTime

class OrderTable(tag: Tag) extends Table[Order](tag, Some("scalademo"), "orders") {

  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  /** The userId column */
  def userId = column[Long]("userId")

  /** The totalPrice column */
  def totalPrice = column[BigDecimal]("totalPrice")

  /** The orderDate column */
  def orderDate = column[LocalDateTime]("orderDate")

  /**
   * This is the table's default "projection".
   * It defines how the columns are converted to and from the User object.
   * In this case, we are simply passing the id, name, email and password parameters to the User case classes
   * apply and unapply methods.
   */
  def * =
    (id, userId, totalPrice, orderDate) <> ((Order.apply _).tupled, Order.unapply)
}