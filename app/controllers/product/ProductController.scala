package controllers.product

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.models.Product
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.doubleFormat
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ ProductService, UserService} //ExternalProductService
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class ProductFormInput(productName: String, price: Double, expDate: String)

/**
 * Takes HTTP requests and produces JSON.
 */
class ProductController @Inject() (cc: ControllerComponents,
                                productService: ProductService,
                               // extProductService: ExternalProductService,
                                userService: UserService,
                                silhouette: Silhouette[JWTEnvironment])
                               (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "productName" -> nonEmptyText(maxLength = 128),
        "price" -> of[Double],
        "expDate" -> nonEmptyText
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("User", "Operator", "Admin")).async { implicit request =>
      logger.trace(s"getById: $id")
      productService.find(id).map {
        case Some(product) => Ok(Json.toJson(ProductResource.fromProduct(product)))
        case None => NotFound
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("User", "Operator", "Admin")).async { implicit request =>
      logger.trace("getAllProducts")
      productService.listAll().map { products =>
        Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product))))
      }
    }
//
//  def create: Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Creator")).async { implicit request =>
//      logger.trace("create Product: ")
//      processJsonProduct(None)
//    }
//
//  def update(id: Long): Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Creator", "Contributor")).async { implicit request =>
//      logger.trace(s"update Product id: $id")
//      processJsonProduct(Some(id))
//    }
//
//  def delete(id: Long): Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Creator")).async { implicit request =>
//      logger.trace(s"Delete product: id = $id")
//      productService.delete(id).map { deletedCnt =>
//        if (deletedCnt == 1) Ok(JsString(s"Delete product $id successfully"))
//        else BadRequest(JsString(s"Unable to delete product $id"))
//      }
//    }
//
//  def getAllExternal: Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("User", "Creator", "Contributor")).async { implicit request =>
//      logger.trace("getAll External Products")
//
//      // try/catch Future exception with transform
//      extProductService.listAll().transform {
//        case Failure(exception) => handleExternalError(exception)
//        case Success(products => Try(Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product)))))
//      }
//    }
//
//  def createExternal: Action[AnyContent] =
//    SecuredAction(WithRole[JWTAuthenticator]("Creator")).async { implicit request =>
//      logger.trace("create External Product: ")
//
//      def failure(badForm: Form[ProductFormInput]) = {
//        Future.successful(BadRequest(JsString("Invalid Input")))
//      }
//
//      def success(input: ProductFormInput) = {
//        // create a product from given form input
//        val product = null;// Product(Some(999L), input.author, input.title, input.content, LocalDateTime.now(), input.description)
//
//        extProductService.save(product).transform {
//          case Failure(exception) => handleExternalError(exception)
//          case Success(product) => Try(Created(Json.toJson(ProductResource.fromProduct(product))))
//        }
//      }
//
//      form.bindFromRequest().fold(failure, success)
//    }
//
//  private def handleExternalError(throwable: Throwable): Try[Result] = {
//    throwable match {
//      case ese: ExternalServiceException =>
//        logger.trace(s"An ExternalServiceException occurred: ${ese.getMessage}")
//        if (ese.error.isEmpty)
//          Try(BadRequest(JsString(s"An ExternalServiceException occurred. statusCode: ${ese.statusCode}")))
//        else Try(BadRequest(Json.toJson(ese.error.get)))
//      case _ =>
//        logger.trace(s"An other exception occurred on getAllExternal: ${throwable.getMessage}")
//        Try(BadRequest(JsString("Unable to create an external product")))
//    }
//  }
//
//  private def processJsonProduct[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {
//
//    def failure(badForm: Form[ProductFormInput]) = {
//      Future.successful(BadRequest(JsString("Invalid Input")))
//    }
//
//    def success(input: ProductFormInput) = {
//      // create a product from given form input
//      val product = null;// = Product(id)
//
//      productService.save(product).map { product =>
//        Created(Json.toJson(ProductResource.fromProduct(product)))
//      }
//    }
//
//    form.bindFromRequest().fold(failure, success)
//  }
}