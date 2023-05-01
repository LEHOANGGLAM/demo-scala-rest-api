package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import domain.dao.{OrderDao, UserDao}
import domain.models.{Order, User}

import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[OrderServiceImpl])
trait OrderService  {


  def find(id: Long): Future[Option[Order]]


  def listAll(): Future[Iterable[Order]]


  def save(order: Order): Future[Order]


  def update(order: Order): Future[Order]


  def delete(id: Long): Future[Int]
}


@Singleton
class OrderServiceImpl @Inject() (orderDao: OrderDao)(implicit ex: ExecutionContext) extends OrderService {
  override def find(id: Long): Future[Option[Order]] = orderDao.find(id)

  override def listAll(): Future[Iterable[Order]] = orderDao.listAll()

  override def save(order: Order): Future[Order] = orderDao.save(order)

  override def update(order: Order): Future[Order] = orderDao.update(order)

  override def delete(id: Long): Future[Int] = orderDao.delete(id)
}

