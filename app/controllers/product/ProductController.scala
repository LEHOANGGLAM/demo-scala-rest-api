package controllers.product

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.auth.SilhouetteControllerComponents
import domain.models.Product
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.doubleFormat
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ExternalProductService, ProductService}
import utils.auth.{JWTEnvironment, WithProvider, WithRole}
import utils.logging.RequestMarkerContext

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class ProductFormInput(productName: String, price: Double, expDate: LocalDateTime)

/**
 * Takes HTTP requests and produces JSON.
 */
class ProductController @Inject()(cc: ControllerComponents,
                                  productService: ProductService,
                                  extProductService: ExternalProductService,
                                  silhouette: Silhouette[JWTEnvironment])
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithProvider[JWTAuthenticator](CredentialsProvider.ID)).async { implicit request =>
      logger.trace(s"getById: $id")
      productService.find(id).map {
        case Some(product) => Ok(Json.toJson(ProductResource.fromProduct(product)))
        case None => NotFound
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithProvider[JWTAuthenticator](CredentialsProvider.ID)).async { implicit request =>
      logger.trace("getAllProducts")
      productService.listAll().map { products =>
        Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product))))
      }
    }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Operator", "Admin")).async { implicit request =>
      logger.trace("create Product")
      processJsonProduct(None)
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Operator", "Admin")).async { implicit request =>
      logger.trace("update Product")
      logger.trace(s"update Post id: $id")
      processJsonProduct(Some(id))
    }

  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Operator", "Admin")).async { implicit request =>
      logger.trace("delete Product")
      productService.delete(id).map { deletedCnt =>
        if (deletedCnt == 1) Ok(JsString(s"Delete post $id successfully"))
        else BadRequest(JsString(s"Unable to delete post $id"))
      }
    }

  def getExternalProducts: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Operator", "Admin")).async { implicit request =>
      logger.trace("getAll External Products")

      // try/catch Future exception with transform
      extProductService.listAll().transform {
        case Failure(exception) => handleExternalError(exception)
        case Success(products) => Try(
          Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product))))
       )
      }
    }
  private def handleExternalError(throwable: Throwable): Try[Result] = {
    throwable match {
      case ese: ExternalServiceException =>
        logger.trace(s"An ExternalServiceException occurred: ${ese.getMessage}")
        if (ese.error.isEmpty)
          Try(BadRequest(JsString(s"An ExternalServiceException occurred. statusCode: ${ese.statusCode}")))
        else Try(BadRequest(Json.toJson(ese.error.get)))
      case _ =>
        logger.trace(s"An other exception occurred on getAllExternal: ${throwable.getMessage}")
        Try(BadRequest(JsString("Unable to create an external post")))
    }
  }

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "productName" -> nonEmptyText(maxLength = 128),
        "price" -> of[Double],
        "expDate" -> localDateTime("yyyy-MM-dd'T'HH:mm:ss")
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  private def processJsonProduct[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[ProductFormInput]) = {
      Future.successful(BadRequest(JsString("Invalid Input")))
    }

    def success(input: ProductFormInput) = {
      // create a post from given form input
      val pro = Product(id, input.productName, input.price, input.expDate)

      if (id.isDefined) {
        productService.update(pro).map { pro =>
          Created(Json.toJson(ProductResource.fromProduct(pro)))
        }
      }else{
        productService.save(pro).map { pro =>
          Created(Json.toJson(ProductResource.fromProduct(pro)))
        }
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}