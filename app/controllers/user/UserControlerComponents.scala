//package controllers.user
//
//import play.api.http.FileMimeTypes
//import play.api.i18n.{Langs, MessagesApi}
//import play.api.mvc._
//import services.{ProductService, UserService}
//
//import javax.inject.Inject
//
///**
// * A wrapped request for post resources.
// *
// * This is commonly used to hold request-specific information like security credentials, and useful shortcut methods.
// */
//trait UserRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
//
//class UserRequest[A](request: Request[A], val messagesApi: MessagesApi)
//  extends WrappedRequest(request) with UserRequestHeader
//
///**
// * Packages up the component dependencies for the post controller.
// *
// * This is a good way to minimize the surface area exposed to the controller, so the
// * controller only has to have one thing injected.
// */
//case class UserControlerComponents @Inject()(userService: UserService,
//                                                actionBuilder: DefaultActionBuilder,
//                                                parsers: PlayBodyParsers,
//                                                messagesApi: MessagesApi,
//                                                langs: Langs,
//                                                fileMimeTypes: FileMimeTypes,
//                                                executionContext: scala.concurrent.ExecutionContext)
//  extends ControllerComponents
//
