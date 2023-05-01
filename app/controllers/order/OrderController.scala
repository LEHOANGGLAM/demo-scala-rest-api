package controllers.order

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.models.{Order, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.{bigDecimalFormat, longFormat}
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{OrderService, UserService}
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class OrderFormInput(userId: Long, totalPrice: BigDecimal)

/**
 * Takes HTTP requests and produces JSON.
 */
class OrderController @Inject()(cc: ControllerComponents,
                                orderService: OrderService,
                                  userService: UserService,
                                  silhouette: Silhouette[JWTEnvironment])
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  private val form: Form[OrderFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "userId" -> of[Long],
        "totalPrice" -> of[BigDecimal]
      )(OrderFormInput.apply)(OrderFormInput.unapply)
    )
  }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]( "Admin")).async { implicit request =>
      logger.trace(s"getById: $id")
      orderService.find(id).map {
        case Some(order) => Ok(Json.toJson(OrderResource.fromOrder(order)))
        case None => NotFound
      }
    }

//  def getAll: Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
//      logger.trace("getAllUsers")
//      userService.listAll().map { users =>
//        Ok(Json.toJson(users.map(user => UserResource.fromUser(user))))
//      }
//    }
//
//  def create: Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
//      logger.trace("create User")
//      processJsonUser(None)
//    }
//
//  def update(id: Long): Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
//      logger.trace("update User")
//      logger.trace(s"update User id: $id")
//      processJsonUser(Some(id))
//    }
//
//  def delete(email: String): Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]( "Admin")).async { implicit request =>
//      logger.trace("delete User")
//      userService.delete(email).map { deletedCnt =>
//        if (deletedCnt == 1) Ok(JsString(s"Delete user $email successfully"))
//        else BadRequest(JsString(s"Unable to delete user $email"))
//      }
//    }

  private def processJsonOrder[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[OrderFormInput]) = {
      Future.successful(BadRequest(JsString("Invalid Input")))
    }

    def success(input: OrderFormInput) = {
      // create a order from given form input
      val order = Order(id,input.userId, input.totalPrice, LocalDateTime.now())

      orderService.save(order).map { order =>
        Created(Json.toJson(OrderResource.fromOrder(order)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}