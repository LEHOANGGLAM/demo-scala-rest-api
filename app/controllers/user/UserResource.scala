package controllers.user

import domain.models.{ User}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

/**
 * DTO for displaying post information.
 */
case class UserResource(id: Long,
                           email: String,
                           role: String,
                           firstName: String,
                           lastName: String,
                           password: Option[String] = None,
                           address: String,
                           phoneNumber: String,
                           birthDate: LocalDateTime)

object UserResource {

  /**
   * Mapping to read/write a PostResource out as a JSON value.
   */
  implicit val format: OFormat[UserResource] = Json.format[UserResource]

  def fromUser(user: User): UserResource =
    UserResource(user.id.getOrElse(-1), user.email, user.role, user.firstName,
      user.lastName, user.password, user.address, user.phoneNumber, user.birthDate)
}