//package controllers.product
//
//import play.api.http.{FileMimeTypes, HttpVerbs}
//import play.api.i18n.{Langs, MessagesApi}
//import play.api.mvc._
//import play.api.{Logger, MarkerContext}
//import services.{  ProductService, UserService}//ExternalProductService
//
//import javax.inject.Inject
//import scala.concurrent.{ExecutionContext, Future}
//
///**
// * A wrapped request for post resources.
// *
// * This is commonly used to hold request-specific information like security credentials, and useful shortcut methods.
// */
//trait ProductRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
//
//class ProductRequest[A](request: Request[A], val messagesApi: MessagesApi)
//  extends WrappedRequest(request) with ProductRequestHeader
//
///**
// * Packages up the component dependencies for the post controller.
// *
// * This is a good way to minimize the surface area exposed to the controller, so the
// * controller only has to have one thing injected.
// */
//case class ProductControlerComponents @Inject()(//externalPostService: ExternalProductService,
//                                                productService: ProductService,
//                                                userService: UserService,
//                                                actionBuilder: DefaultActionBuilder,
//                                                parsers: PlayBodyParsers,
//                                                messagesApi: MessagesApi,
//                                                langs: Langs,
//                                                fileMimeTypes: FileMimeTypes,
//                                                executionContext: scala.concurrent.ExecutionContext)
//  extends ControllerComponents
//
