package controllers.order

import domain.models.{Order, User}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

/**
 * DTO for displaying post information.
 */
case class OrderResource(id: Long, userId: Long, totalPrice: BigDecimal, orderDate: LocalDateTime)

object OrderResource {

  /**
   * Mapping to read/write a PostResource out as a JSON value.
   */
  implicit val format: OFormat[OrderResource] = Json.format[OrderResource]

  def fromOrder(order: Order): OrderResource =
    OrderResource(order.id.getOrElse(-1), order.userId, order.totalPrice, order.orderDate)
}