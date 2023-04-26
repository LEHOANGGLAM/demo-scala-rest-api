package domain.tables


import domain.models.Product
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

class ProductTable(tag: Tag) extends Table[Product](tag, Some("scalademo"), "products") {

  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  /** The productName column */
  def productName = column[String]("productName")

  /** The price column */
  def price = column[Double]("price")

  /** The expDate column */
  def expDate = column[LocalDateTime]("expDate")

  /**
   * This is the table's default "projection".
   * It defines how the columns are converted to and from the User object.
   * In this case, we are simply passing the id, name, email and password parameters to the User case classes
   * apply and unapply methods.
   */
  def * =
    (id, productName, price,expDate) <> ((Product.apply _).tupled, Product.unapply)
}