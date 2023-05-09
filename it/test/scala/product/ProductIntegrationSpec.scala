package product

import controllers.product.ProductResource
import domain.models.{Product, User}
import fixtures.DataFixtures
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.test._
import play.api.libs.ws._
import play.api.test.Helpers._

class ProductIntegrationSpec extends DataFixtures with MockitoSugar with ScalaFutures {

  case class LoginBody(email: String, password: String)
  implicit val format: OFormat[LoginBody] = Json.format[LoginBody]
  val authHeaderKey: String = "X-Auth"

  override protected def beforeAll(): Unit = {
    // insert data before tests
    createUsers(Users.allUsers)
    createProducts(Products.allProducts)
  }

  "GET /products/:id" should {

    "get a product successfully" in new WithServer(app) {

      val product: Product = Products.product1
      val user: User = Users.admin
      val loginBody: LoginBody = LoginBody(user.email, Users.plainPassword)

      // login to get access token
      val loginRes: WSResponse = await(WsTestClient.wsUrl("/signIn").post(Json.toJson(loginBody)))
      val accessToken: Option[String] = loginRes.header(authHeaderKey)
      accessToken.isEmpty mustBe false

      // Execute test and then extract result
      val getProductRes: WSResponse = await(
        WsTestClient.wsUrl("/v1/products/1")
          .addHttpHeaders(authHeaderKey -> accessToken.get)
          .get()
      )

      // verify result after test
      getProductRes.status mustEqual 200
      val actualProduct: ProductResource = getProductRes.body[JsValue].as[ProductResource]
      verifyProduct(actualProduct, product)
    }
  }

  private def verifyProduct(actual: ProductResource, expected: Product): Unit = {
    actual.id mustEqual expected.id.get
    actual.name mustEqual expected.productName
    actual.price mustEqual expected.price
    actual.expDate mustEqual expected.expDate
  }
}
