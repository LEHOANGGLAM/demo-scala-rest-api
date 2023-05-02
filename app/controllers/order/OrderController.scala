package controllers.order

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.models.{Order, OrderItem, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.{bigDecimalFormat, doubleFormat, intFormat, longFormat}
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{OrderItemService, OrderService, UserService}
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class OrderItemFormInput(productId: Long, price: Double, quantity: Int)
case class OrderFormInput(userId: Long, totalPrice: BigDecimal, items: Seq[OrderItemFormInput])

/**
 * Takes HTTP requests and produces JSON.
 */
class OrderController @Inject()(cc: ControllerComponents,
                                orderService: OrderService,
                                orderItemService: OrderItemService,
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
        "totalPrice" -> of[BigDecimal],
        "items" -> seq(
          mapping(
            "productId" -> of[Long],
            "price" -> of[Double],
            "quantity" -> of[Int]
          )(OrderItemFormInput.apply)(OrderItemFormInput.unapply)
        )
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

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("getAllOrders")
      orderService.listAll().map { orders =>
        Ok(Json.toJson(orders.map(order => OrderResource.fromOrder(order))))
      }
    }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("create Order")
      processJsonOrder(None)
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("update Order")
      logger.trace(s"update Order id: $id")
      processJsonOrder(Some(id))
    }

  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]( "Admin")).async { implicit request =>
      logger.trace("delete Order")
      orderService.delete(id).map { deletedCnt =>
        if (deletedCnt == 1) Ok(JsString(s"Delete order $id successfully"))
        else BadRequest(JsString(s"Unable to delete order $id"))
      }
    }

  private def processJsonOrder[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[OrderFormInput]) = {
      Future.successful(BadRequest(JsString("Invalid Input")))
    }

    def success(input: OrderFormInput) = {
      // create a order from given form input
      val order = Order(id ,input.userId, input.totalPrice, LocalDateTime.now())
//      val orderItems = input.items.map(item =>
//        OrderItem(productId = item.productId, price = item.price, quantity = item.quantity, orderId = id)
//      )

      orderService.save(order).flatMap { savedOrder =>

        orderItemService.saveAll(null).map { _ => //orderItems
          Created(Json.toJson(OrderResource.fromOrder(savedOrder)))
        }
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

}