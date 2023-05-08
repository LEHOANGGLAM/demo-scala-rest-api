package controllers.order

import domain.models.{ OrderItem}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

/**
 * DTO for displaying post information.
 */
case class OrderItemResource(id: Long, orderId: Long, productId: Long, price: Double, quantity: Int)

object OrderItemResource {

  implicit val format: OFormat[OrderItemResource] =
    Json.format[OrderItemResource]

  def fromOrderItem(orderItem: OrderItem): OrderItemResource =
    OrderItemResource(orderItem.id.getOrElse(-1),
      orderItem.orderId,
      orderItem.productId,
      orderItem.price,
      orderItem.quantity
    )
}

