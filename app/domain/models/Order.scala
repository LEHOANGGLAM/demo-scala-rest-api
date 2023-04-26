package domain.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

/**
 * The order class
 */
case class Order(id: Option[Long], userId: Long, totalPrice: BigDecimal, orderDate: LocalDateTime) {
  object Order {

    /**
     * Mapping to read/write a PostResource out as a JSON value.
     */
    implicit val format: OFormat[Order] = Json.format[Order]
  }
}


