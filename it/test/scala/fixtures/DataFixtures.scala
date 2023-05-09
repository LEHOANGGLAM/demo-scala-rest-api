package fixtures

import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import domain.dao.{ProductDao, UserDao}
import domain.models.{Product, User}
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration._

abstract class DataFixtures extends PlaySpec with AbstractPersistenceTests {

  val productDao: ProductDao = get[ProductDao]
  val userDao: UserDao = get[UserDao]

  def createUsers(users: Seq[User]): Unit = {
    Await.result(userDao.saveAll(users), 5.seconds)
  }

  def createProducts(products: Seq[Product]): Unit = {
    Await.result(productDao.saveAll(products), 5.seconds)
  }

  object Users {
    val plainPassword: String = "fakeP@ssw0rd";
    val password: String = new BCryptSha256PasswordHasher().hash(plainPassword).password

    val admin = User(Some(2L), "email@gmail.com", "Admin", "firstname", "lastname", Some("password"), "address", "phone", LocalDateTime.now())
    val user = User(Some(2L), "email2", "User", "firstname", "lastname", Some("password"), "address", "phone", LocalDateTime.now())
    val operator = User(Some(2L), "email3", "Operator", "firstname", "lastname", Some("password"), "address", "phone", LocalDateTime.now())

    val allUsers: Seq[User] = Seq(admin, user, operator)
  }

  object Products {

    val product1: Product = Product(Some(1L), "product 1", 10.0, LocalDateTime.now())
    val product2: Product = Product(Some(2L), "product 2", 10.0, LocalDateTime.now())
    val product3: Product = Product(Some(3L), "product 3", 10.0, LocalDateTime.now())
    val product4: Product = Product(Some(4L), "product 4", 10.0, LocalDateTime.now())
    val allProducts: Seq[Product] = Seq(product1, product2, product3, product4)
  }
}
