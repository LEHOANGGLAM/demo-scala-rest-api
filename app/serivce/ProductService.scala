package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import domain.dao.{ProductDao, UserDao}
import domain.models.{Product, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * UserService is a type of an IdentityService that can be used to authenticate users.
 */
@ImplementedBy(classOf[ProductServiceImpl])
trait ProductService {

  def find(id: Long): Future[Option[Product]]

  def listAll(): Future[Iterable[Product]]

  def save(product: Product): Future[Product]

  def update(product: Product): Future[Product]

  def delete(id: Long): Future[Int]
}

@Singleton
class ProductServiceImpl @Inject()(productDao: ProductDao)(implicit ex: ExecutionContext) extends ProductService {

  override def find(id: Long): Future[Option[Product]] = productDao.find(id)

  override def listAll(): Future[Iterable[Product]] = productDao.listAll()

  override def save(product: Product): Future[Product] = productDao.save(product)

  override def update(product: Product): Future[Product] = productDao.update(product)

  override def delete(id: Long): Future[Int] = productDao.delete(id)
}

