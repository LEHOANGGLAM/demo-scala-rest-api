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
import scala.concurrent.{Await, ExecutionContext, Future}

case class OrderItemFormInput(id: Option[Long], productId: Long, price: Double, quantity: Int)

case class OrderFormInput(userId: Long, items: Seq[OrderItemFormInput])

/**
 * Takes HTTP requests and produces JSON.
 */
class OrderController @Inject()(cc: ControllerComponents,
                                orderService: OrderService,
                                orderItemService: OrderItemService,
                                silhouette: Silhouette[JWTEnvironment])
                               (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace(s"get an order by id: $id")
      orderService.find(id).map {
        case Some(order) => {
          request.identity.role match {
            case "Admin" => Ok(Json.toJson(toOrderResource(order)))
            case "User" => {
              if (order.userId == request.identity.id.get) {
                Ok(Json.toJson(toOrderResource(order)))
              } else {
                Forbidden
              }
            }
            case _ => Forbidden
          }
        }
        case None => NotFound
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("get all orders")
      orderService.listAll().map { orders =>
        Ok(Json.toJson(orders.map(order => toOrderResource(order))))
      }
    }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async {
      implicit request => {
        logger.trace("create new order")
        processJsonOrder(None)
      }
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request => {
      request.identity.role match {
        case "Admin" => processJsonOrder(Some(id))
        case "User" =>
          orderService.find(id).flatMap {
            case Some(order) => {
              if (order.userId != request.identity.id.get) {
                Future.successful(Forbidden)
              } else {
                processJsonOrder(Some(id))
              }
            }
            case None => Future.successful(NotFound)
          }
        case _ => Future.successful(Forbidden)
      }
    }
    }


  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace(s"delete order with: id = $id")

      request.identity.role match {
        case "Admin" =>
          orderService.delete(id).map { results =>
            if (results == 1)
              Ok(JsString(s"delete order with id: $id successfully"))
            else BadRequest(JsString(s"unable to delete order with $id"))
          }
        case "User" =>
          orderService.find(id).map {
            case Some(order) =>
              if (order.userId == request.identity.id.get) {
                orderService.delete(id)
                Ok(JsString(s"delete order with id: $id successfully"))
              } else {
                Forbidden
              }
            case None => NotFound
          }
      }
    }

  private val orderItemForm = {
    import play.api.data.Forms._

    mapping(
      "id" -> optional(longNumber),
      "productId" -> longNumber,
      "price" -> of(doubleFormat),
      "quantity" -> number
    )(OrderItemFormInput.apply)(OrderItemFormInput.unapply)
  }

  private val formPost: Form[OrderFormInput] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "userId" -> longNumber,
        "items" -> seq(orderItemForm)
      )(OrderFormInput.apply)(OrderFormInput.unapply)
    )
  }

  private def processJsonOrder[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[OrderFormInput]) = {
      Future.successful(BadRequest(JsString("Invalid Input")))
    }

    def success(input: OrderFormInput) = {
      if (id.isDefined) {
        var totalPrice: BigDecimal = 0;
        val orderItems = input.items.map(orderItem => {
          totalPrice = totalPrice + orderItem.price * orderItem.quantity
          OrderItem(
            orderItem.id,
            id.get,
            orderItem.productId,
            orderItem.price,
            orderItem.quantity
          )
        })
        val order =
          Order(id, input.userId, totalPrice, LocalDateTime.now())

        orderService.update(order, orderItems).map { order =>
          Created(Json.toJson(toOrderResource(order)))
        }
      } else {
        var totalPrice: BigDecimal = 0;
        val orderItems = input.items.map(orderItem => {
          totalPrice = totalPrice + orderItem.price * orderItem.quantity
          OrderItem(
            None,
            0,
            orderItem.productId,
            orderItem.price,
            orderItem.quantity
          )
        })
        val order =
          Order(id, input.userId, totalPrice, LocalDateTime.now())
        orderService.save(order, orderItems).map { order =>
          Created(Json.toJson(toOrderResource(order)))
        }
      }
    }

    formPost.bindFromRequest().fold(failure, success)
  }

  private def toOrderResource(order: Order): OrderResource = {
    val orderItems = Await.result(
      orderItemService.findByOrderId(order.id.get),
      scala.concurrent.duration.Duration.Inf
    )
    var orderItemsResource = Seq[OrderItemResource]()
    orderItems.foreach(
      orderDetail =>
        orderItemsResource = orderItemsResource :+ OrderItemResource
          .fromOrderItem(orderDetail)
    )
    OrderResource.fromOrder(order).copy(orderItems = orderItemsResource)
  }
}