package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import domain.dao.{OrderDao, OrderItemDao, UserDao}
import domain.models.{Order, OrderItem, User}

import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[OrderServiceImpl])
trait OrderItemService  {


  def find(id: Long): Future[Option[OrderItem]]


  def listAll(): Future[Iterable[OrderItem]]


  def save(orderItem: OrderItem): Future[OrderItem]

  def saveAll(list: Seq[OrderItem]): Future[Seq[OrderItem]]

  def update(orderItem: OrderItem): Future[OrderItem]


  def delete(id: Long): Future[Int]
}


@Singleton
class OrderItemServiceImpl @Inject() (orderItemDao: OrderItemDao)(implicit ex: ExecutionContext) extends OrderItemService {
  override def find(id: Long): Future[Option[OrderItem]] = orderItemDao.find(id)

  override def listAll(): Future[Iterable[OrderItem]] = orderItemDao.listAll()

  override def save(orderItem: OrderItem): Future[OrderItem] = orderItemDao.save(orderItem)

  override def saveAll(list: Seq[OrderItem]): Future[Seq[OrderItem]] = orderItemDao.saveAll(list)

  override def update(orderItem: OrderItem): Future[OrderItem] = orderItemDao.update(orderItem)

  override def delete(id: Long): Future[Int] = orderItemDao.delete(id)
}

