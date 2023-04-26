package domain.models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import java.time.LocalDateTime

/**
 * The User class
 */
case class User(id: Option[Long], email: String, role: String, firstName: String, lastName: String,
                 password: Option[String] = None, address: String, phoneNumber: String, birthDate: LocalDateTime)
  extends Identity {

  /**
   * Generates login info from email
   *
   * @return login info
   */
  def loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, email)

  /**
   * Generates password info from password.
   *
   * @return password info
   */
  def passwordInfo: PasswordInfo = PasswordInfo(BCryptSha256PasswordHasher.ID, password.get)
}