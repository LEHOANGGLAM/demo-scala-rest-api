package domain.models

import play.api.libs.json.{Json, OFormat}

/**
 * The OrderItem class
 */
case class OrderItem(id: Option[Long], orderId: Long, productId: Long, price: Double, quantity: Int) {
  object OrderItem {

    /**
     * Mapping to read/write a PostResource out as a JSON value.
     */
    implicit val format: OFormat[OrderItem] = Json.format[OrderItem]
  }
}


