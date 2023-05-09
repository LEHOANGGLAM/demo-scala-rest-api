package services

import domain.dao.{ UserDao}
import domain.models.User
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class UserServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  val mockUserDao: UserDao = mock[UserDao]

  val userService: UserService = new UserServiceImpl(mockUserDao)(global)

  "UserService#find(id: Long)" should {
    "get a user successfully" in {

      val user = User(Some(2L), "email", "Admin", "firstname", "lastname", Option("password"), "address", "phone", LocalDateTime.now())
      when(mockUserDao.find(anyLong())).thenReturn(Future.successful(Some(user)))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      actual.id.get mustEqual user.id.get
      actual.email mustEqual user.email
      actual.role mustEqual user.role
      actual.firstName mustEqual user.firstName
      actual.lastName mustEqual user.lastName
      actual.address mustEqual user.address
    }

    "user not found" in {
      when(mockUserDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

}
