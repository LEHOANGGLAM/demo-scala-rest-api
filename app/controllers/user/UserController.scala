package controllers.user

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import domain.models.{Product, User}
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.doubleFormat
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ProductService, UserService}
import utils.auth.{JWTEnvironment, WithProvider, WithRole}
import utils.logging.RequestMarkerContext

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class UserFormInput(email: String, role: String, firstName: String, lastName: String, password: Option[String] = None,
                         address: String, phoneNumber: String, birthDate: LocalDateTime)

/**
 * Takes HTTP requests and produces JSON.
 */
class UserController @Inject()(cc: ControllerComponents,
                                  userService: UserService,
                                  silhouette: Silhouette[JWTEnvironment])
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  private val form: Form[UserFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email" -> nonEmptyText(maxLength = 128),
        "role" -> nonEmptyText(maxLength = 128),
        "firstName" -> nonEmptyText(maxLength = 128),
        "lastName" -> nonEmptyText(maxLength = 128),
        "password" -> optional(text(maxLength = 128)),
        "address" -> nonEmptyText(maxLength = 128),
        "phoneNumber" -> nonEmptyText(maxLength = 128),
        "birthDate" -> localDateTime("yyyy-MM-dd'T'HH:mm:ss")
      )(UserFormInput.apply)(UserFormInput.unapply)
    )
  }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]( "Admin")).async { implicit request =>
      logger.trace(s"getById: $id")
      userService.find(id).map {
        case Some(user) => Ok(Json.toJson(UserResource.fromUser(user)))
        case None => NotFound
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("getAllUsers")
      userService.listAll().map { users =>
        Ok(Json.toJson(users.map(user => UserResource.fromUser(user))))
      }
    }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("create User")
      processJsonUser(None)
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("update User")
      logger.trace(s"update User id: $id")
      processJsonUser(Some(id))
    }

  def delete(email: String): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]( "Admin")).async { implicit request =>
      logger.trace("delete User")
      userService.delete(email).map { deletedCnt =>
        if (deletedCnt == 1) Ok(JsString(s"Delete user $email successfully"))
        else BadRequest(JsString(s"Unable to delete user $email"))
      }
    }

  private def processJsonUser[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[UserFormInput]) = {
      Future.successful(BadRequest(JsString("Invalid Input")))
    }

    def success(input: UserFormInput) = {
      // create a user from given form input
      val user = User(id, input.email, input.role, input.firstName,
        input.lastName,input.password, input.address, input.phoneNumber, input.birthDate)

      userService.save(user).map { user =>
        Created(Json.toJson(UserResource.fromUser(user)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}