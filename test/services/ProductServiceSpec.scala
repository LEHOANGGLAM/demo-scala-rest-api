package services

import domain.dao.ProductDao
import domain.models.Product
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ProductServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  val mockProductDao: ProductDao = mock[ProductDao]

  val productService: ProductService = new ProductServiceImpl(mockProductDao)(global)

  "ProductService#find(id: Long)" should {
    "get a product successfully" in {
      val product = Product(Some(2L), "Product Name 222", 22.0, LocalDateTime.now())
      when(mockProductDao.find(anyLong())).thenReturn(Future.successful(Some(product)))

      val result = productService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      actual.id.get mustEqual product.id.get
      actual.productName mustEqual product.productName
      actual.price mustEqual product.price
      actual.expDate mustEqual product.expDate
    }

    "product not found" in {
      when(mockProductDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = productService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  "ProductService#listAll()" should {
    "get all products successfully" in {
      val product2 = Product(Some(2L), "Product Name 222", 22.0, LocalDateTime.now())
      val product1 = Product(Some(1L), "Product Name 111", 22.0, LocalDateTime.now())
      val products = Seq(product1, product2)

      when(mockProductDao.listAll()).thenReturn(Future.successful(products))

      val result = productService.listAll().futureValue
      result.isEmpty mustBe false
      result.size mustBe 2
      result.map(_.id.get) must contain allOf(1L, 2L)
    }
  }

  "ProductService#save(product)" should {
    "save a product successfully" in {
      val product3 = Product(Some(3L), "Product Name 222", 22.0, LocalDateTime.now())
      when(mockProductDao.save(product3)).thenReturn(Future.successful(product3))

      val result = productService.save(product3).futureValue

      result.id.get mustEqual product3.id.get
      result.productName mustEqual product3.productName
      result.price mustEqual product3.price
      result.expDate mustEqual product3.expDate
    }
  }

  "ProductService#update(product)" should {
    "update a product successfully" in {
      val product3 = Product(Some(3L), "Product Name 333", 22.0, LocalDateTime.now())
      when(mockProductDao.update(product3)).thenReturn(Future.successful(product3))

      val result = productService.update(product3).futureValue
      result.id.get mustEqual product3.id.get
      result.productName mustEqual product3.productName
      result.price mustEqual product3.price
      result.expDate mustEqual product3.expDate
    }
  }

  "ProductService#delete(id: Long)" should {
    "delete a product successfully" in {
      when(mockProductDao.delete(3L)).thenReturn(Future.successful(1))

      val result = productService.delete(3L).futureValue
      result mustEqual true
    }
  }
}
