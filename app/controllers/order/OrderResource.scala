package controllers.order

import domain.models.{Order, OrderItem}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

/**
 * DTO for displaying post information.
 */
case class OrderResource(id: Long, userId: Long, totalPrice: BigDecimal, orderDate: LocalDateTime,  orderItems: Seq[OrderItemResource])

object OrderResource {

  implicit val format: OFormat[OrderResource] =
    Json.format[OrderResource]

  def fromOrder(order: Order): OrderResource =
    OrderResource(order.id.getOrElse(-1),
      order.userId,
      order.totalPrice,
      order.orderDate,
      List[OrderItemResource]()
    )
}

